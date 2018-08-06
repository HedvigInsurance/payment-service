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
import com.hedvig.paymentservice.domain.trustlyOrder.commands.PayoutResponseReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.PendingNotificationReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.SelectAccountResponseReceivedCommand;
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
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aggregate
public class TrustlyOrder {

  Logger log = LoggerFactory.getLogger(TrustlyOrder.class);

  @AggregateIdentifier private UUID id;
  private String trustlyOrderId;
  private OrderType orderType;
  private OrderState orderState;
  private String memberId;
  private UUID externalTransactionId;
  private List<Error> errors = new ArrayList<Error>();
  private TreeSet<String> handledNotifications = new TreeSet<>();

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
  public TrustlyOrder(CreatePayoutOrderCommand cmd) {
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
  public void cmd(PayoutResponseReceivedCommand cmd) {
    apply(new OrderAssignedTrustlyIdEvent(cmd.getHedvigOrderId(), cmd.getTrustlyOrderId()));
    apply(
        new PayoutResponseReceivedEvent(
            cmd.getHedvigOrderId(), memberId, cmd.getAmount(), externalTransactionId));
  }

  @CommandHandler
  public void cmd(PaymentErrorReceivedCommand cmd) {
    apply(new PaymentErrorReceivedEvent(cmd.getHedvigOrderId(), cmd.getError()));
  }

  @CommandHandler
  public void cmd(PayoutErrorReceivedCommand cmd) {
    apply(new PayoutErrorReceivedEvent(cmd.getHedvigOrderId(), cmd.getError()));
  }

  @CommandHandler
  public void cmd(AccountNotificationReceivedCommand cmd) {

    if (handledNotifications.contains(cmd.getNotificationId())) {
      return;
    }

    apply(
        new AccountNotificationReceivedEvent(
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
            cmd.getZipCode()));
    markOrderComplete();
  }

  @CommandHandler
  public void cmd(CancelNotificationReceivedCommand cmd) {
    apply(new OrderCanceledEvent(this.id));
  }

  @CommandHandler
  public void cmd(PendingNotificationReceivedCommand cmd) {
    apply(
        new PendingNotificationReceivedEvent(
            cmd.getHedvigOrderId(),
            cmd.getNotificationId(),
            cmd.getTrustlyOrderId(),
            cmd.getAmount(),
            cmd.getMemberId(),
            cmd.getTimestamp()));
  }

  @CommandHandler
  public void cmd(CreditNotificationReceivedCommand cmd) {
    apply(
        new CreditNotificationReceivedEvent(
            this.id,
            this.externalTransactionId,
            cmd.getNotificationId(),
            cmd.getTrustlyOrderId(),
            cmd.getMemberId(),
            cmd.getAmount(),
            cmd.getTimestamp(),
            this.orderType));

    markOrderComplete();
  }

  public void markOrderComplete() {
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
