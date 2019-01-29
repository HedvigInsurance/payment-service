package com.hedvig.paymentservice.domain.payments.sagas;

import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountCreatedEvent;
import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountUpdatedEvent;
import com.hedvig.paymentservice.domain.registerAccount.commands.ReceiveRegisterAccountConfirmationCommand;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.saga.EndSaga;
import org.axonframework.eventhandling.saga.SagaEventHandler;
import org.axonframework.eventhandling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Saga
public class DirectDebitAccountSaga {

  @Autowired
  transient CommandGateway commandGateway;

  private static final String HEDVIG_ORDER_ID = "hedvigOrderId";

  @StartSaga
  @SagaEventHandler(associationProperty = HEDVIG_ORDER_ID)
  @EndSaga
  public void on(TrustlyAccountCreatedEvent e) {
    commandGateway.sendAndWait(new ReceiveRegisterAccountConfirmationCommand(e.getHedvigOrderId(), e.getMemberId()));
  }

  @StartSaga
  @SagaEventHandler(associationProperty = HEDVIG_ORDER_ID)
  @EndSaga
  public void on(TrustlyAccountUpdatedEvent e) {
    commandGateway.sendAndWait(new ReceiveRegisterAccountConfirmationCommand(e.getHedvigOrderId(), e.getMemberId()));
  }
}
