package com.hedvig.paymentservice.domain.payments;

import com.hedvig.paymentservice.domain.payments.commands.ChargeCompletedCommand;
import com.hedvig.paymentservice.domain.payments.commands.CreateChargeCommand;
import com.hedvig.paymentservice.domain.payments.commands.CreateTrustlyAccountCommand;
import com.hedvig.paymentservice.domain.payments.commands.PayoutCompletedCommand;
import com.hedvig.paymentservice.domain.payments.events.ChargeCreatedEvent;
import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountCreatedEvent;
import java.util.ArrayList;
import java.util.List;
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
                cmd.getUnit(),
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
                cmd.getUnit(),
                cmd.getTimestamp(),
                "direct debit mandate not received in Trustly"
            ));
            return;
        }

        apply(new ChargeCreatedEvent(
            this.id,
            cmd.getTransactionId(),
            cmd.getAmount(),
            cmd.getUnit(),
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
        // apply(new ChargeEvent(
        //     this.id,
        //     cmd.getAmount(),
        //     cmd.getCurrency(),
        //     cmd.getTimestamp()
        // ));
    }

    @CommandHandler
    public void cmd(PayoutCompletedCommand cmd) {
        apply(new PayoutEvent(
            this.id,
            cmd.getAmount(),
            cmd.getCurrency(),
            cmd.getTimestamp()
        ));
    }

    @EventSourcingHandler
    public void on(TrustlyAccountCreatedEvent e) {
        trustlyAccountId = e.getTrustlyAccountId();
    }
}
