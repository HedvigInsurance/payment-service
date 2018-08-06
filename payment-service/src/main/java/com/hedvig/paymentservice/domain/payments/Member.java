package com.hedvig.paymentservice.domain.payments;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

import com.hedvig.paymentservice.domain.payments.commands.ChargeCompletedCommand;
import com.hedvig.paymentservice.domain.payments.commands.ChargeFailedCommand;
import com.hedvig.paymentservice.domain.payments.commands.CreateChargeCommand;
import com.hedvig.paymentservice.domain.payments.commands.CreateMemberCommand;
import com.hedvig.paymentservice.domain.payments.commands.CreatePayoutCommand;
import com.hedvig.paymentservice.domain.payments.commands.PayoutCompletedCommand;
import com.hedvig.paymentservice.domain.payments.commands.PayoutFailedCommand;
import com.hedvig.paymentservice.domain.payments.commands.UpdateTrustlyAccountCommand;
import com.hedvig.paymentservice.domain.payments.events.ChargeCompletedEvent;
import com.hedvig.paymentservice.domain.payments.events.ChargeCreatedEvent;
import com.hedvig.paymentservice.domain.payments.events.ChargeCreationFailedEvent;
import com.hedvig.paymentservice.domain.payments.events.ChargeFailedEvent;
import com.hedvig.paymentservice.domain.payments.events.MemberCreatedEvent;
import com.hedvig.paymentservice.domain.payments.events.PayoutCompletedEvent;
import com.hedvig.paymentservice.domain.payments.events.PayoutCreatedEvent;
import com.hedvig.paymentservice.domain.payments.events.PayoutCreationFailedEvent;
import com.hedvig.paymentservice.domain.payments.events.PayoutFailedEvent;
import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountCreatedEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.val;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aggregate
public class Member {
  Logger log = LoggerFactory.getLogger(Member.class);

  @AggregateIdentifier private String id;

  private List<Transaction> transactions = new ArrayList<>();
  private TrustlyAccount trustlyAccount;

  public Member() {}

  @CommandHandler
  public Member(CreateMemberCommand cmd) {
    apply(new MemberCreatedEvent(cmd.getMemberId()));
  }

  @CommandHandler
  public boolean cmd(CreateChargeCommand cmd) {
    if (trustlyAccount == null) {
      log.info("Cannot charge account - no account set up in Trustly");
      apply(
          new ChargeCreationFailedEvent(
              this.id,
              cmd.getTransactionId(),
              cmd.getAmount(),
              cmd.getTimestamp(),
              "account id not set"));
      return false;
    }
    if (trustlyAccount.isDirectDebitMandateActive() == false) {
      log.info("Cannot charge account - direct debit mandate not received in Trustly");
      apply(
          new ChargeCreationFailedEvent(
              this.id,
              cmd.getTransactionId(),
              cmd.getAmount(),
              cmd.getTimestamp(),
              "direct debit mandate not received in Trustly"));
      return false;
    }

    apply(
        new ChargeCreatedEvent(
            this.id,
            cmd.getTransactionId(),
            cmd.getAmount(),
            cmd.getTimestamp(),
            this.trustlyAccount.getAccountId(),
            cmd.getEmail()));
    return true;
  }

  @CommandHandler
  public boolean cmd(CreatePayoutCommand cmd) {
    if (trustlyAccount == null) {
      log.info("Cannot payout account - no account set up in Trustly");
      apply(
          new PayoutCreationFailedEvent(
              id, cmd.getTransactionId(), cmd.getAmount(), cmd.getTimestamp()));
      return false;
    }

    apply(
        new PayoutCreatedEvent(
            id,
            cmd.getTransactionId(),
            cmd.getAmount(),
            cmd.getAddress(),
            cmd.getCountryCode(),
            cmd.getDateOfBirth(),
            cmd.getFirstName(),
            cmd.getLastName(),
            cmd.getTimestamp(),
            trustlyAccount.getAccountId()));
    return true;
  }

  @CommandHandler
  public void cmd(UpdateTrustlyAccountCommand cmd) {

    apply(
        new TrustlyAccountCreatedEvent(
            this.id,
            cmd.getHedvigOrderId(),
            cmd.getAccountId(),
            cmd.getAddress(),
            cmd.getBank(),
            cmd.getCity(),
            cmd.getClearingHouse(),
            cmd.getDescriptor(),
            cmd.isDirectDebitMandateActive(),
            cmd.getLastDigits(),
            cmd.getName(),
            cmd.getPersonId(),
            cmd.getZipCode()));
  }

  @CommandHandler
  public void cmd(ChargeCompletedCommand cmd) {
    val transaction = getSingleTransaction(transactions, cmd.getTransactionId(), id);
    if (transaction.getAmount().equals(cmd.getAmount()) == false) {
      log.error(
          "CRITICAL: Transaction amounts differ for transactionId: {} - our amount: {}, amount from payment provider: {}",
          transaction.getAmount().toString(),
          cmd.getAmount().toString(),
          transaction.getTransactionId().toString());
      throw new RuntimeException("Transaction amount mismatch");
    }
    apply(
        new ChargeCompletedEvent(
            this.id, cmd.getTransactionId(), cmd.getAmount(), cmd.getTimestamp()));
  }

  @CommandHandler
  public void cmd(ChargeFailedCommand cmd) {
    val transaction = getSingleTransaction(transactions, cmd.getTransactionId(), id);
    if (transaction == null) {
      final String s =
          String.format(
              "Could not find matching transaction for ChargeFailedCommand with memberId: %s and transactionId: %s",
              id, cmd.getTransactionId());
      throw new RuntimeException(s);
    }
    apply(new ChargeFailedEvent(this.id, cmd.getTransactionId()));
  }

  @CommandHandler
  public void cmd(PayoutCompletedCommand cmd) {
    val transaction = getSingleTransaction(transactions, cmd.getTransactionId(), id);
    if (transaction.getAmount().equals(cmd.getAmount()) == false) {
      log.error(
          "CRITICAL: Transaction amounts differ for transactionId: {} - our amount: {}, amount from payment provider: {}",
          transaction.getAmount().toString(),
          cmd.getAmount().toString(),
          transaction.getTransactionId().toString());
      throw new RuntimeException("Transaction amount mismatch");
    }
    apply(new PayoutCompletedEvent(id, cmd.getTransactionId(), cmd.getTimestamp()));
  }

  @CommandHandler
  public void cmd(PayoutFailedCommand cmd) {
    apply(new PayoutFailedEvent(id, cmd.getTransactionId(), cmd.getAmount(), cmd.getTimestamp()));
  }

  @EventSourcingHandler
  public void on(MemberCreatedEvent e) {
    id = e.getMemberId();
  }

  @EventSourcingHandler
  public void on(ChargeCreatedEvent e) {
    transactions.add(
        new Transaction(
            e.getTransactionId(),
            e.getAmount(),
            e.getTimestamp(),
            TransactionType.CHARGE,
            TransactionStatus.INITIATED));
  }

  @EventSourcingHandler
  public void on(PayoutCreatedEvent e) {
    transactions.add(
        new Transaction(
            e.getTransactionId(),
            e.getAmount(),
            e.getTimestamp(),
            TransactionType.PAYOUT,
            TransactionStatus.INITIATED));
  }

  @EventSourcingHandler
  public void on(ChargeCompletedEvent e) {
    val transaction = getSingleTransaction(transactions, e.getTransactionId(), id);
    transaction.setTransactionStatus(TransactionStatus.COMPLETED);
  }

  @EventSourcingHandler
  public void on(ChargeFailedEvent w) {
    val transaction = getSingleTransaction(transactions, w.getTransactionId(), id);
    transaction.setTransactionStatus(TransactionStatus.FAILED);
  }

  @EventSourcingHandler
  public void on(PayoutCompletedEvent e) {
    val transaction = getSingleTransaction(transactions, e.getTransactionId(), id);
    transaction.setTransactionStatus(TransactionStatus.COMPLETED);
  }

  @EventSourcingHandler
  public void on(PayoutFailedEvent e) {
    val transaction = getSingleTransaction(transactions, e.getTransactionId(), id);
    transaction.setTransactionStatus(TransactionStatus.FAILED);
  }

  @EventSourcingHandler
  public void on(TrustlyAccountCreatedEvent e) {

    val account = new TrustlyAccount(e.getTrustlyAccountId(), e.isDirectDebitMandateActivated());

    this.trustlyAccount = account;
  }

  private static Transaction getSingleTransaction(
      List<Transaction> transactions, UUID transactionId, String memberId) {
    val matchingTransactions =
        transactions
            .stream()
            .filter(t -> t.getTransactionId().equals(transactionId))
            .collect(Collectors.toList());
    if (matchingTransactions.size() != 1) {
      throw new RuntimeException(
          String.format(
              "Unexpected number of matching transactions: %n, with transactionId: %s for memberId: %s",
              matchingTransactions.size(), transactionId.toString(), memberId));
    }

    return matchingTransactions.get(0);
  }
}
