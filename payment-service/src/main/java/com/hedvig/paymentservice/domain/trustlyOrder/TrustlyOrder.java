package com.hedvig.paymentservice.domain.trustlyOrder;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;
import com.hedvig.paymentService.trustly.data.response.Error;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.AccountNotificationReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.CancelNotificationReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.CreateOrderCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.CreatePaymentOrderCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.CreatePayoutOrderCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.CreditNotificationReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.PaymentErrorReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.PaymentResponseReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.PayoutErrorReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.PendingNotificationReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.SelectAccountResponseReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.TrustlyPayoutResponseReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.events.AccountNotificationReceivedEvent;
import com.hedvig.paymentservice.domain.trustlyOrder.events.CreditNotificationReceivedEvent;
import com.hedvig.paymentservice.domain.trustlyOrder.events.ExternalTransactionIdAssignedEvent;
import com.hedvig.paymentservice.domain.trustlyOrder.events.NotificationReceivedEvent;
import com.hedvig.paymentservice.domain.trustlyOrder.events.OrderAssignedTrustlyIdEvent;
import com.hedvig.paymentservice.domain.trustlyOrder.events.OrderCanceledEvent;
import com.hedvig.paymentservice.domain.trustlyOrder.events.OrderCompletedEvent;
import com.hedvig.paymentservice.domain.trustlyOrder.events.OrderCreatedEvent;
import com.hedvig.paymentservice.domain.trustlyOrder.events.PaymentErrorReceivedEvent;
import com.hedvig.paymentservice.domain.trustlyOrder.events.PaymentResponseReceivedEvent;
import com.hedvig.paymentservice.domain.trustlyOrder.events.PayoutErrorReceivedEvent;
import com.hedvig.paymentservice.domain.trustlyOrder.events.PayoutResponseReceivedEvent;
import com.hedvig.paymentservice.domain.trustlyOrder.events.PendingNotificationReceivedEvent;
import com.hedvig.paymentservice.domain.trustlyOrder.events.SelectAccountResponseReceivedEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;

@Slf4j
@Aggregate
public class TrustlyOrder {

    @AggregateIdentifier
    private UUID id;
    private String trustlyOrderId;
    private OrderType orderType;
    private OrderState orderState;
    private String memberId;
    private UUID externalTransactionId;
    private List<Error> errors = new ArrayList<Error>();
    private TreeSet<String> handledNotifications = new TreeSet<>();

    public TrustlyOrder() {
    }

    @CommandHandler
    public void handle(CreateOrderCommand command) {
        apply(new OrderCreatedEvent(command.getHedvigOrderId(), command.getMemberId()));
    }

    @CommandHandler
    public void handle(CreatePaymentOrderCommand command) {
        apply(new OrderCreatedEvent(command.getHedvigOrderId(), command.getMemberId()));
        apply(new ExternalTransactionIdAssignedEvent(command.getHedvigOrderId(), command.getTransactionId(), command.getMemberId()));
    }

    @CommandHandler
    public void handle(CreatePayoutOrderCommand command) {
        apply(new OrderCreatedEvent(command.getHedvigOrderId(), command.getMemberId()));
        apply(new ExternalTransactionIdAssignedEvent(command.getHedvigOrderId(), command.getTransactionId(), command.getMemberId()));
    }

    @CommandHandler
    public void handle(SelectAccountResponseReceivedCommand command) {
        apply(new OrderAssignedTrustlyIdEvent(command.getHedvigOrderId(), command.getTrustlyOrderId()));
        apply(new SelectAccountResponseReceivedEvent(command.getHedvigOrderId(), command.getIframeUrl()));
    }

    @CommandHandler
    public void handle(PaymentResponseReceivedCommand command) {
        apply(new OrderAssignedTrustlyIdEvent(command.getHedvigOrderId(), command.getTrustlyOrderId()));
        apply(new PaymentResponseReceivedEvent(command.getHedvigOrderId(), command.getUrl()));
    }

    @CommandHandler
    public void handle(TrustlyPayoutResponseReceivedCommand command) {
        apply(new OrderAssignedTrustlyIdEvent(command.getHedvigOrderId(), command.getTrustlyOrderId()));
        apply(
            new PayoutResponseReceivedEvent(
                command.getHedvigOrderId(), memberId, command.getAmount(), externalTransactionId));
    }

    @CommandHandler
    public void handle(PaymentErrorReceivedCommand command) {
        apply(new PaymentErrorReceivedEvent(command.getHedvigOrderId(), command.getError()));
    }

    @CommandHandler
    public void handle(PayoutErrorReceivedCommand command) {
        apply(new PayoutErrorReceivedEvent(command.getHedvigOrderId(), command.getError()));
    }

    @CommandHandler
    public void handle(AccountNotificationReceivedCommand command) {
        if (handledNotifications.contains(command.getNotificationId())) {
            return;
        }

        apply(
            new AccountNotificationReceivedEvent(
                this.id,
                this.memberId,
                command.getNotificationId(),
                command.getTrustlyOrderId(),
                command.getAccountId(),
                command.getAddress(),
                command.getBank(),
                command.getCity(),
                command.getClearingHouse(),
                command.getDescriptor(),
                command.getDirectDebitMandateActivated(),
                command.getLastDigits(),
                command.getName(),
                command.getPersonId(),
                command.getZipCode()
            )
        );
        markOrderComplete();
    }

    @CommandHandler
    public void handle(CancelNotificationReceivedCommand command) {
        apply(new OrderCanceledEvent(this.id));
    }

    @CommandHandler
    public void handle(PendingNotificationReceivedCommand command) {
        apply(
            new PendingNotificationReceivedEvent(
                command.getHedvigOrderId(),
                command.getNotificationId(),
                command.getTrustlyOrderId(),
                command.getAmount(),
                command.getMemberId(),
                command.getTimestamp()));
    }

    @CommandHandler
    public void handle(CreditNotificationReceivedCommand command) {
        apply(
            new CreditNotificationReceivedEvent(
                this.id,
                this.externalTransactionId,
                command.getNotificationId(),
                command.getTrustlyOrderId(),
                command.getMemberId(),
                command.getAmount(),
                command.getTimestamp(),
                this.orderType));

        markOrderComplete();
    }

    private void markOrderComplete() {
        if (orderState == OrderState.CONFIRMED) {
            apply(new OrderCompletedEvent(this.id));
        }
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
        this.orderState = OrderState.STARTED;
    }

    @EventSourcingHandler
    public void on(PaymentResponseReceivedEvent e) {
        this.orderType = OrderType.CHARGE;
    }

    @EventSourcingHandler
    public void on(PayoutResponseReceivedEvent e) {
        orderType = OrderType.ACCOUNT_PAYOUT;
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
    public void on(AccountNotificationReceivedEvent e) {
        handledNotifications.add(e.getNotificationId());
    }

    @EventSourcingHandler
    public void on(NotificationReceivedEvent e) {
        handledNotifications.add(e.getNotificationId());
    }
}
