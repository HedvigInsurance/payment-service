package com.hedvig.paymentservice.domain.trustlyOrder;

import com.hedvig.paymentservice.domain.trustlyOrder.commands.CreateTrustlySelectAccountOrderCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.SelectAccountResponseReceviedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.events.TrustlyOrderCreatedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.UUID;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Aggregate
public class TrustlyOrder {

    @AggregateIdentifier
    private UUID id;

    public TrustlyOrder() {}

    @CommandHandler
    public TrustlyOrder(CreateTrustlySelectAccountOrderCommand cmd) {

        apply(new TrustlyOrderCreatedEvent(cmd.getRequestId()));
    }

    @CommandHandler
    public void cmd(SelectAccountResponseReceviedCommand cmd) {

    }


    @EventSourcingHandler
    public void on(TrustlyOrderCreatedEvent e) {
        this.id = e.getHedvigOrderId();
    }

}
