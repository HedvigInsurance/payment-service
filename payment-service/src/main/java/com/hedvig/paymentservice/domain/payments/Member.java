package com.hedvig.paymentservice.domain.payments;

import com.hedvig.paymentservice.domain.payments.commands.ChargeCompletedCommand;
import com.hedvig.paymentservice.domain.payments.commands.CreateChargeCommand;
import com.hedvig.paymentservice.domain.payments.commands.CreateTrustlyAccountCommand;
import com.hedvig.paymentservice.domain.payments.events.ChargeCompletedEvent;
import com.hedvig.paymentservice.domain.payments.events.ChargeCreatedEvent;
import com.hedvig.paymentservice.domain.payments.events.ChargeCreationFailedEvent;
import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountCreatedEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.val;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Aggregate
public class Member {
    Logger log = LoggerFactory.getLogger(Member.class);

    @AggregateIdentifier
    private String id;

    private String trustlyAccountId;
    private boolean directDebitActive;

    private List<Transaction> transactions = new ArrayList<Transaction>();

    public Member() {}

    @CommandHandler
    public void cmd(CreateChargeCommand cmd) {
        if (trustlyAccountId == null) {
            log.info("Cannot charge account - no account set up in Trustly");
            apply(new ChargeCreationFailedEvent(
                this.id,
                cmd.getTransactionId(),
                cmd.getAmount(),
                cmd.getTimestamp(),
                "account id not set"
            ));
            return;
        }
        if (directDebitActive == false) {
            log.info("Cannot charge account - direct debit mandate not received in Trustly");
            apply(new ChargeCreationFailedEvent(
                this.id,
                cmd.getTransactionId(),
                cmd.getAmount(),
                cmd.getTimestamp(),
                "direct debit mandate not received in Trustly"
            ));
            return;
        }

        apply(new ChargeCreatedEvent(
            this.id,
            cmd.getTransactionId(),
            cmd.getAmount(),
            cmd.getTimestamp(),
            this.trustlyAccountId,
            cmd.getEmail()
        ));
    }

    @CommandHandler
    public void cmd(CreateTrustlyAccountCommand cmd) {
        apply(new TrustlyAccountCreatedEvent(
            this.id,
            cmd.getTrustlyAccountId()
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
        trustlyAccountId = e.getTrustlyAccountId();
    }
}
