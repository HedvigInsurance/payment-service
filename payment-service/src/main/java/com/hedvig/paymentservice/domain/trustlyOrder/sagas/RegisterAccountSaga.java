package com.hedvig.paymentservice.domain.trustlyOrder.sagas;

import com.hedvig.paymentservice.domain.registerAccount.commands.ReceiveRegisterAccountNotificationCommand;
import com.hedvig.paymentservice.domain.registerAccount.commands.ReceiveRegisterAccountResponseCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.events.AccountNotificationReceivedEvent;
import com.hedvig.paymentservice.domain.trustlyOrder.events.SelectAccountResponseReceivedEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.saga.EndSaga;
import org.axonframework.eventhandling.saga.SagaEventHandler;
import org.axonframework.eventhandling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Saga
public class RegisterAccountSaga {

  @Autowired
  transient CommandGateway commandGateway;

  private static final String HEDVIG_ORDER_ID = "hedvigOrderId";

  @StartSaga
  @SagaEventHandler(associationProperty = HEDVIG_ORDER_ID)
  @EndSaga
  public void on(SelectAccountResponseReceivedEvent e) {
    commandGateway.sendAndWait(new ReceiveRegisterAccountResponseCommand(e.getHedvigOrderId(), e.getIframeUrl()));
  }

  @StartSaga
  @SagaEventHandler(associationProperty = HEDVIG_ORDER_ID)
  @EndSaga
  public void on(AccountNotificationReceivedEvent e) {
    commandGateway.sendAndWait(new ReceiveRegisterAccountNotificationCommand(e.getHedvigOrderId(), e.getMemberId()));
  }
}
