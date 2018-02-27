package com.hedvig.paymentservice.domain.trustlyOrder;

import com.hedvig.paymentservice.domain.trustlyOrder.commands.AccountNotificationReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.CreateAccountCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.events.AccountNotificationReceivedEvent;
import com.hedvig.paymentservice.domain.trustlyOrder.events.TrustlyAccountCreatedEvent;
import com.hedvig.paymentservice.domain.trustlyOrder.events.TrustlyAccountUpdatedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Aggregate
public class TrustlyAccount {

    private final Logger log = LoggerFactory.getLogger(TrustlyAccount.class);

    @AggregateIdentifier
    private String id;

    public TrustlyAccount() {

    }

    @CommandHandler
    public TrustlyAccount(CreateAccountCommand cmd) {
        apply(new TrustlyAccountCreatedEvent(cmd.getAccountId()));
    }

    @CommandHandler
    public void on(AccountNotificationReceivedCommand cmd) {
        log.debug("Got account notification received command!");

        final AccountNotificationReceivedEvent event = cmd.getEvent();
        final TrustlyAccountUpdatedEvent trustlyAccountUpdatedEvent = new TrustlyAccountUpdatedEvent(
                cmd.getAccountId(),
                event.getAddress(),
                event.getBank(),
                event.getCity(),
                event.getClearingHouse(),
                event.getDescriptor(),
                event.getDirectDebitMandate(),
                event.getLastDigits(),
                event.getName(),
                event.getPersonId(),
                event.getZipCode()
        );

        apply(trustlyAccountUpdatedEvent);
    }


    @EventSourcingHandler
    public void on(TrustlyAccountCreatedEvent event) {
        this.id = event.getAccountId();
    }
}
