package com.hedvig.paymentservice.domain.trustlyOrder.sagas;

import com.hedvig.paymentservice.domain.payments.commands.ChargeCompletedCommand;
import com.hedvig.paymentservice.domain.payments.commands.ChargeFailedCommand;
import com.hedvig.paymentservice.domain.payments.commands.PayoutFailedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.events.CreditNotificationReceivedEvent;
import com.hedvig.paymentservice.domain.trustlyOrder.events.ExternalTransactionIdAssignedEvent;
import com.hedvig.paymentservice.domain.trustlyOrder.events.OrderCanceledEvent;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.saga.EndSaga;
import org.axonframework.eventhandling.saga.SagaEventHandler;
import org.axonframework.eventhandling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Saga
public class CreditSaga {
  @Autowired transient CommandGateway commandGateway;

  String memberId;
  private UUID transactionId;

  @StartSaga
  @SagaEventHandler(associationProperty = "hedvigOrderId")
  public void on(ExternalTransactionIdAssignedEvent e) {
    this.transactionId = e.getTransactionId();
    this.memberId = e.getMemberId();
  }

  @StartSaga
  @SagaEventHandler(associationProperty = "hedvigOrderId")
  @EndSaga
  public void on(CreditNotificationReceivedEvent e) {
    this.memberId = e.getMemberId();
    switch (e.getOrderType()) {
      case CHARGE:
        commandGateway.sendAndWait(
            new ChargeCompletedCommand(
                e.getMemberId(), e.getTransactionId(), e.getAmount(), e.getTimestamp()));
        break;
      case ACCOUNT_PAYOUT:
        commandGateway.sendAndWait(
            new PayoutFailedCommand(
                e.getMemberId(), e.getTransactionId(), e.getAmount(), e.getTimestamp()));
        break;
      default:
        throw new RuntimeException(
            "Cannot handle "
                + e.getClass().getName()
                + " with "
                + e.getOrderType().getClass().getName()
                + ": "
                + e.getOrderType().toString());
    }
  }

  @SagaEventHandler(associationProperty = "hedvigOrderId")
  @EndSaga
  public void on(OrderCanceledEvent e) {

      if (this.memberId == null || this.memberId.isEmpty()){
        log.error("CreditSaga - OrderCanceledEvent memberId is null");
        throw new RuntimeException("CreditSaga - OrderCanceledEvent memberId is null");
      }
    commandGateway.sendAndWait(new ChargeFailedCommand(this.memberId, this.transactionId));
  }
}
