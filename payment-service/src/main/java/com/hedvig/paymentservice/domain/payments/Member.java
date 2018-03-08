package com.hedvig.paymentservice.domain.payments;

import com.hedvig.paymentservice.domain.payments.commands.ChargeCompletedCommand;
import com.hedvig.paymentservice.domain.payments.commands.CreateChargeCommand;
import com.hedvig.paymentservice.domain.payments.commands.CreateMemberCommand;
import com.hedvig.paymentservice.domain.payments.commands.UpdateTrustlyAccountCommand;
import com.hedvig.paymentservice.domain.payments.events.*;
import lombok.val;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Aggregate
public class Member {
    Logger log = LoggerFactory.getLogger(Member.class);

    @AggregateIdentifier
    private String id;


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
            apply(new ChargeCreationFailedEvent(
                this.id,
                cmd.getTransactionId(),
                cmd.getAmount(),
                cmd.getTimestamp(),
                "account id not set"
            ));
            return false;
        }
        if (trustlyAccount.isDirectDebitMandateActive() == false) {
            log.info("Cannot charge account - direct debit mandate not received in Trustly");
            apply(new ChargeCreationFailedEvent(
                this.id,
                cmd.getTransactionId(),
                cmd.getAmount(),
                cmd.getTimestamp(),
                "direct debit mandate not received in Trustly"
            ));
            return false;
        }

        apply(new ChargeCreatedEvent(
            this.id,
            cmd.getTransactionId(),
            cmd.getAmount(),
            cmd.getTimestamp(),
            this.trustlyAccount.getAccountId(),
            cmd.getEmail()
        ));
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
                cmd.getZipCode()
        ));
    }

    @CommandHandler
    public void cmd(ChargeCompletedCommand cmd) {
        apply(new ChargeCompletedEvent(
            this.id,
            cmd.getTransactionId(),
            cmd.getAmount(),
            cmd.getTimestamp()
        ));
    }

    @EventSourcingHandler
    public void on(MemberCreatedEvent e) {
        this.id = e.getMemberId();
    }

    @EventSourcingHandler
    public void on(ChargeCompletedEvent e) {
        val matchingTransactions = transactions
            .stream()
            .filter(t -> t.getTransactionId().equals(e.getTransactionId()))
            .collect(Collectors.toList());
        if (matchingTransactions.size() != 1) {
            throw new RuntimeException(
                String.format(
                    "Unexpected number of matching transactions: %n, with transactionId: %s for memberId: %s",
                    matchingTransactions.size(),
                    e.getTransactionId().toString(),
                    this.id
                )
            );
        }

        val transaction = matchingTransactions.get(0);
        transaction.setTransactionStatus(TransactionStatus.COMPLETED);
    }

    @EventSourcingHandler
    public void on(TrustlyAccountCreatedEvent e) {

        val account = new TrustlyAccount(e.getTrustlyAccountId(), e.isDirectDebitMandateActivated());

        this.trustlyAccount = account;
    }


}
