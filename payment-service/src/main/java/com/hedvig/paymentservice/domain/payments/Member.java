package com.hedvig.paymentservice.domain.payments;

import com.hedvig.paymentservice.domain.payments.commands.ChargeCompletedCommand;
import com.hedvig.paymentservice.domain.payments.commands.ChargeFailedCommand;
import com.hedvig.paymentservice.domain.payments.commands.CreateChargeCommand;
import com.hedvig.paymentservice.domain.payments.commands.CreateMemberCommand;
import com.hedvig.paymentservice.domain.payments.commands.CreatePayoutCommand;
import com.hedvig.paymentservice.domain.payments.commands.PayoutCompletedCommand;
import com.hedvig.paymentservice.domain.payments.commands.PayoutFailedCommand;
import com.hedvig.paymentservice.domain.payments.commands.UpdateAdyenAccountCommand;
import com.hedvig.paymentservice.domain.payments.commands.UpdateTrustlyAccountCommand;
import com.hedvig.paymentservice.domain.payments.events.AdyenAccountCreatedEvent;
import com.hedvig.paymentservice.domain.payments.events.AdyenAccountUpdatedEvent;
import com.hedvig.paymentservice.domain.payments.events.ChargeCompletedEvent;
import com.hedvig.paymentservice.domain.payments.events.ChargeCreatedEvent;
import com.hedvig.paymentservice.domain.payments.events.ChargeCreationFailedEvent;
import com.hedvig.paymentservice.domain.payments.events.ChargeErroredEvent;
import com.hedvig.paymentservice.domain.payments.events.ChargeFailedEvent;
import com.hedvig.paymentservice.domain.payments.events.DirectDebitConnectedEvent;
import com.hedvig.paymentservice.domain.payments.events.DirectDebitDisconnectedEvent;
import com.hedvig.paymentservice.domain.payments.events.DirectDebitPendingConnectionEvent;
import com.hedvig.paymentservice.domain.payments.events.MemberCreatedEvent;
import com.hedvig.paymentservice.domain.payments.events.PayoutCompletedEvent;
import com.hedvig.paymentservice.domain.payments.events.PayoutCreatedEvent;
import com.hedvig.paymentservice.domain.payments.events.PayoutCreationFailedEvent;
import com.hedvig.paymentservice.domain.payments.events.PayoutErroredEvent;
import com.hedvig.paymentservice.domain.payments.events.PayoutFailedEvent;
import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountCreatedEvent;
import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountUpdatedEvent;
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberResult;
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberResultType;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Aggregate
@Slf4j
public class Member {

  @AggregateIdentifier
  private String id;

  private List<Transaction> transactions = new ArrayList<>();
  private TrustlyAccount trustlyAccount;
  private AdyenAccount adyenAccount;

  public Member() {
  }

  @CommandHandler
  public Member(CreateMemberCommand cmd) {
    apply(new MemberCreatedEvent(cmd.getMemberId()));
  }

  @CommandHandler
  public ChargeMemberResult cmd(CreateChargeCommand cmd) {

    if (trustlyAccount == null) {
      log.info("Cannot charge account - no account set up in Trustly");
      apply(
        new ChargeCreationFailedEvent(
          this.id,
          cmd.getTransactionId(),
          cmd.getAmount(),
          cmd.getTimestamp(),
          "account id not set"));
      return new ChargeMemberResult(cmd.getTransactionId(), ChargeMemberResultType.NO_TRUSTLY_ACCOUNT);
    }

    if (!trustlyAccount.getDirectDebitStatus().equals(DirectDebitStatus.CONNECTED)) {
      log.info("Cannot charge account - direct debit mandate not received in Trustly");
      apply(
        new ChargeCreationFailedEvent(
          this.id,
          cmd.getTransactionId(),
          cmd.getAmount(),
          cmd.getTimestamp(),
          "direct debit mandate not received in Trustly"));
      return new ChargeMemberResult(cmd.getTransactionId(), ChargeMemberResultType.NO_DIRECT_DEBIT);
    }

    apply(
      new ChargeCreatedEvent(
        this.id,
        cmd.getTransactionId(),
        cmd.getAmount(),
        cmd.getTimestamp(),
        this.trustlyAccount.getAccountId(),
        cmd.getEmail(),
        cmd.getCreatedBy()
      ));
    return new ChargeMemberResult(cmd.getTransactionId(), ChargeMemberResultType.SUCCESS);
  }

  @CommandHandler
  public boolean cmd(CreatePayoutCommand cmd) {
    if (trustlyAccount == null) {
      log.info("Cannot payout account - no account set up in Trustly");
      apply(
        new PayoutCreationFailedEvent(id, cmd.getTransactionId(), cmd.getAmount(), cmd.getTimestamp())
      );
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
        trustlyAccount.getAccountId(),
        cmd.getCategory(),
        cmd.getReferenceId(),
        cmd.getNote(),
        cmd.getHandler()
      )
    );
    return true;
  }

  @CommandHandler
  public void cmd(UpdateTrustlyAccountCommand cmd) {
    if (trustlyAccount == null || !trustlyAccount.getAccountId().equals(cmd.getAccountId())) {
      apply(TrustlyAccountCreatedEvent.fromUpdateTrustlyAccountCmd(this.id, cmd));
    } else {
      apply(TrustlyAccountUpdatedEvent.fromUpdateTrustlyAccountCmd(this.id, cmd));
    }
    updateDirectDebitStatus(cmd);
  }

  @CommandHandler
  public void cmd(UpdateAdyenAccountCommand cmd) {
    if (adyenAccount == null || adyenAccount.getAdyenTokenId().equals(cmd.getAdyenTokenId())) {
      apply(new AdyenAccountCreatedEvent(cmd.getMemberId(), cmd.getAdyenTokenId(), cmd.getRecurringDetailReference(), cmd.getTokenStatus()));
    } else {
      apply(new AdyenAccountUpdatedEvent(cmd.getMemberId(), cmd.getAdyenTokenId(), cmd.getRecurringDetailReference(), cmd.getTokenStatus()));
    }
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

      apply(
        new ChargeErroredEvent(
          cmd.getMemberId(),
          cmd.getTransactionId(),
          cmd.getAmount(),
          String.format("Transaction amounts differ (expected %s but was %s)", transaction.getAmount(), cmd.getAmount()),
          cmd.getTimestamp()
        ));

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
    if (!transaction.getAmount().equals(cmd.getAmount())) {
      log.error(
        "CRITICAL: Transaction amounts differ for transactionId: {} - our amount: {}, amount from payment provider: {}",
        transaction.getAmount().toString(),
        cmd.getAmount().toString(),
        transaction.getTransactionId().toString());

      apply(
        new PayoutErroredEvent(
          cmd.getMemberId(),
          cmd.getTransactionId(),
          cmd.getAmount(),
          String.format("Transaction amounts differ (expected %s but was %s)", transaction.getAmount(), cmd.getAmount()),
          cmd.getTimestamp()
        ));

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
    final Transaction tx = new Transaction(
      e.getTransactionId(),
      e.getAmount(),
      e.getTimestamp(),
      TransactionType.CHARGE,
      TransactionStatus.INITIATED);

    transactions.add(tx);
  }

  @EventSourcingHandler
  public void on(PayoutCreatedEvent e) {
    Transaction tx = new Transaction(
      e.getTransactionId(),
      e.getAmount(),
      e.getTimestamp(),
      TransactionType.PAYOUT,
      TransactionStatus.INITIATED);
    transactions.add(tx);
  }

  @EventSourcingHandler
  public void on(ChargeCompletedEvent e) {
    val tx = getSingleTransaction(transactions, e.getTransactionId(), id);
    tx.setTransactionStatus(TransactionStatus.COMPLETED);
  }

  @EventSourcingHandler
  public void on(ChargeFailedEvent e) {
    val tx = getSingleTransaction(transactions, e.getTransactionId(), id);
    tx.setTransactionStatus(TransactionStatus.FAILED);
  }

  @EventSourcingHandler
  public void on(PayoutCompletedEvent e) {
    val tx = getSingleTransaction(transactions, e.getTransactionId(), id);
    tx.setTransactionStatus(TransactionStatus.COMPLETED);
  }

  @EventSourcingHandler
  public void on(PayoutFailedEvent e) {
    val transaction = getSingleTransaction(transactions, e.getTransactionId(), id);
    transaction.setTransactionStatus(TransactionStatus.FAILED);
  }

  @EventSourcingHandler
  public void on(TrustlyAccountCreatedEvent e) {
    this.trustlyAccount = new TrustlyAccount(e.getTrustlyAccountId(), null);
  }

  @EventSourcingHandler
  public void on(DirectDebitConnectedEvent e) {
    this.trustlyAccount.setDirectDebitStatus(DirectDebitStatus.CONNECTED);
  }

  @EventSourcingHandler
  public void on(DirectDebitDisconnectedEvent e) {
    this.trustlyAccount.setDirectDebitStatus(DirectDebitStatus.DISCONNECTED);
  }

  @EventSourcingHandler
  public void on(DirectDebitPendingConnectionEvent e) {
    this.trustlyAccount.setDirectDebitStatus(DirectDebitStatus.PENDING);
  }

  @EventSourcingHandler
  public void on(AdyenAccountCreatedEvent e) {
    this.adyenAccount = new AdyenAccount(e.getAdyenTokenId(), e.getRecurringDetailReference(), e.getTokenStatus());
  }

  @EventSourcingHandler
  public void on(AdyenAccountUpdatedEvent e) {
    this.adyenAccount = new AdyenAccount(e.getAdyenTokenId(), e.getRecurringDetailReference(), e.getTokenStatus());
  }

  private static Transaction getSingleTransaction(
    List<Transaction> transactions, UUID transactionId, String memberId
  ) {
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

  private void updateDirectDebitStatus(UpdateTrustlyAccountCommand cmd) {
    if (cmd.getDirectDebitMandateActive() != null && cmd.getDirectDebitMandateActive()) {
      apply(
        new DirectDebitConnectedEvent(
          this.id,
          cmd.getHedvigOrderId().toString(),
          cmd.getAccountId()));
    } else if (cmd.getDirectDebitMandateActive() != null && !cmd.getDirectDebitMandateActive()) {
      apply(
        new DirectDebitDisconnectedEvent(
          this.id,
          cmd.getHedvigOrderId().toString(),
          cmd.getAccountId()));
    } else {
      if (trustlyAccount.getDirectDebitStatus() == null || (!trustlyAccount.getDirectDebitStatus().equals(DirectDebitStatus.CONNECTED)
        || !trustlyAccount.getDirectDebitStatus().equals(DirectDebitStatus.DISCONNECTED))) {
        apply(
          new DirectDebitPendingConnectionEvent(
            this.id,
            cmd.getHedvigOrderId().toString(),
            cmd.getAccountId()));
      }
    }
  }
}
