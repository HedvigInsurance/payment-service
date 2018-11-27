package com.hedvig.paymentservice.domain.trustlyOrder.sagas;

import com.hedvig.paymentservice.domain.payments.commands.PayoutCompletedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.events.PayoutResponseReceivedEvent;
import java.time.Instant;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.saga.EndSaga;
import org.axonframework.eventhandling.saga.SagaEventHandler;
import org.axonframework.eventhandling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

@Saga
public class PayoutCompleteSaga {
  @Autowired transient CommandGateway commandGateway;

  @StartSaga
  @SagaEventHandler(associationProperty = "hedvigOrderId")
  @EndSaga
  public void on(PayoutResponseReceivedEvent e) {
    commandGateway.sendAndWait(
        new PayoutCompletedCommand(
            e.getMemberId(), e.getTransactionId(), e.getAmount(), Instant.now()));
  }
}
