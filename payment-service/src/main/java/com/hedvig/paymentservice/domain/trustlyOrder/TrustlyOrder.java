
package com.hedvig.paymentservice.domain.trustlyOrder;

import com.hedvig.paymentService.trustly.data.response.Error;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.*;
import com.hedvig.paymentservice.domain.trustlyOrder.events.*;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Aggregate
public class TrustlyOrder {

    Logger log = LoggerFactory.getLogger(TrustlyOrder.class);

    @AggregateIdentifier
    private UUID id;
    private String trustlyOrderId;
    private OrderType orderType;
    private OrderState orderState;
    private String memberId;
    private UUID externalTransactionId;
    private TreeSet<String> notifications = new TreeSet<>();
    private List<Error> errors = new ArrayList<Error>();

    public TrustlyOrder() {}

    @CommandHandler
    public TrustlyOrder(CreateOrderCommand cmd) {

        apply(new OrderCreatedEvent(cmd.getHedvigOrderId(), cmd.getMemberId()));
    }

    @CommandHandler
    public TrustlyOrder(CreatePaymentOrderCommand cmd) {
        apply(new OrderCreatedEvent(cmd.getHedvigOrderId(), cmd.getMemberId()));
        apply(new ExternalTransactionIdAssignedEvent(cmd.getHedvigOrderId(), cmd.getTransactionId()));
    }

    @CommandHandler
    public void cmd(SelectAccountResponseReceivedCommand cmd) {
        apply(new OrderAssignedTrustlyIdEvent(cmd.getHedvigOrderId(), cmd.getTrustlyOrderId()));
        apply(new SelectAccountResponseReceivedEvent(cmd.getHedvigOrderId(), cmd.getIframeUrl()));
    }

    @CommandHandler
    public void cmd(PaymentResponseReceivedCommand cmd) {
        apply(new OrderAssignedTrustlyIdEvent(cmd.getHedvigOrderId(), cmd.getTrustlyOrderId()));
        apply(new PaymentResponseReceivedEvent(cmd.getHedvigOrderId(), cmd.getUrl()));
    }

    @CommandHandler
    public void cmd(PaymentErrorReceivedCommand cmd) {
        apply(new PaymentErrorReceivedEvent(cmd.getHedvigOrderId(), cmd.getError()));
    }

    @CommandHandler
    public void cmd(AccountNotificationReceivedCommand cmd) {
        apply(new AccountNotificationReceivedEvent(
            this.id,
            this.memberId,
            cmd.getNotificationId(),
            cmd.getTrustlyOrderId(),
            cmd.getAccountId(),
            cmd.getAddress(),
            cmd.getBank(),
            cmd.getCity(),
            cmd.getClearingHouse(),
            cmd.getDescriptor(),
            cmd.isDirectDebitMandateActivated(),
            cmd.getLastDigits(),
            cmd.getName(),
            cmd.getPersonId(),
            cmd.getZipCode()
        ));
        apply(new OrderCompletedEvent(this.id));
    }

    @CommandHandler
    public void cmd(CancelNotificationReceivedCommand cmd) {
        apply(new OrderCanceledEvent(this.id));
    }

    @CommandHandler
    public void cmd(CreditNotificationReceivedCommand cmd) {
        apply(new CreditNotificationReceivedEvent(
            this.id,
            this.externalTransactionId,
            cmd.getNotificationId(),
            cmd.getTrustlyOrderId(),
            cmd.getMemberId(),
            cmd.getAmount(),
            cmd.getTimestamp()
        ));
        apply(new OrderCompletedEvent(this.id));
    }

    @EventSourcingHandler
    public void on(OrderCreatedEvent e) {
        this.id = e.getHedvigOrderId();
        this.memberId = e.getMemberId();
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
    public void on(PaymentResponseReceivedEvent e) {
        this.orderType = OrderType.CHARGE;
    }

    @EventSourcingHandler
    public void on(PaymentErrorReceivedEvent e) {
        this.orderType = OrderType.CHARGE;
        this.errors.add(e.getError());
    }

    @EventSourcingHandler
    public void on(OrderCompletedEvent e) {
        this.orderState = OrderState.COMPLETE;
    }

    @EventSourcingHandler
    public void on(OrderCanceledEvent e) {
        this.orderState = OrderState.CANCELED;
    }

    @EventSourcingHandler
    public void on(ExternalTransactionIdAssignedEvent e) {
        this.externalTransactionId = e.getTransactionId();
    }

    @EventSourcingHandler
    public void on(AccountNotificationReceivedCommand cmd) {
    }

    @EventSourcingHandler
    public void on(NotificationReceivedEvent e) {
        notifications.add(e.getNotificationId());
    }
}
