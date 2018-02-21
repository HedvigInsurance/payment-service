
package com.hedvig.paymentservice.domain.trustlyOrder;

import com.hedvig.paymentservice.domain.trustlyOrder.commands.NotificationReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.CreateOrderCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.SelectAccountResponseReceviedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.events.*;
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
    private String trustlyOrderId;
    private OrderType orderType;
    private OrderState orderState;

    public TrustlyOrder() {}

    @CommandHandler
    public TrustlyOrder(CreateOrderCommand cmd) {

        apply(new OrderCreatedEvent(cmd.getHedvigOrderId()));
    }

    @CommandHandler
    public void cmd(SelectAccountResponseReceviedCommand cmd) {
        apply(new OrderAssignedTrustlyIdEvent(cmd.getHedvigOrderId(), cmd.getTrustlyOrderId()));
        apply(new SelectAccountResponseReceivedEvent(cmd.getHedvigOrderId(), cmd.getIframeUrl()));
    }

    @CommandHandler
    public void cmd(NotificationReceivedCommand cmd) {
        apply(new NotificationReceivedEvent(cmd.getNotification().getParams().getData().getNotificationId()));
        switch (cmd.getNotification().getMethod()) {
            case ACCOUNT:
                if(orderState == OrderState.CONFIRMED) {
                    apply(new OrderCompletedEvent(this.id));
                }
                break;
            case CANCEL:
                apply(new OrderCanceledEvent(this.id));
                break;
        }
    }

    @EventSourcingHandler
    public void on(OrderCreatedEvent e) {
        this.id = e.getHedvigOrderId();
    }

    @EventSourcingHandler
    public void on(OrderAssignedTrustlyIdEvent e) {
        this.trustlyOrderId = e.getTrustlyOrderId();
        this.orderState = OrderState.CONFIRMED;
    }

    @EventSourcingHandler
    public void on(SelectAccountResponseReceivedEvent e) {
        this.orderType = OrderType.SELECT_ACCOUNT;
    }

    @EventSourcingHandler
    public void on(OrderCompletedEvent e) {
        this.orderState = OrderState.COMPLETE;
    }

    @EventSourcingHandler
    public void on(OrderCanceledEvent e) {
        this.orderState = OrderState.CANCELED;
    }

}
