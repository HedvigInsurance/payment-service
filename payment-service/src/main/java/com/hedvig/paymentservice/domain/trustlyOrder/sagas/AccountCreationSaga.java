package com.hedvig.paymentservice.domain.trustlyOrder.sagas;

import com.hedvig.paymentservice.domain.payments.commands.CreateMemberCommand;
import com.hedvig.paymentservice.domain.payments.commands.UpdateTrustlyAccountCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.events.AccountNotificationReceivedEvent;
import lombok.val;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.model.AggregateNotFoundException;
import org.axonframework.eventhandling.saga.EndSaga;
import org.axonframework.eventhandling.saga.SagaEventHandler;
import org.axonframework.eventhandling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

@Saga
public class AccountCreationSaga {

  @Autowired transient CommandGateway commandGateway;

  @StartSaga
  @SagaEventHandler(associationProperty = "accountId")
  @EndSaga
  public void on(AccountNotificationReceivedEvent event) {
    try {
      updateTrustlyAccount(event);
    } catch (AggregateNotFoundException e) {
      commandGateway.sendAndWait(new CreateMemberCommand(event.getMemberId()));
      updateTrustlyAccount(event);
    }
  }

  private void updateTrustlyAccount(AccountNotificationReceivedEvent event) {
    val command =
        new UpdateTrustlyAccountCommand(
            event.getMemberId(),
            event.getHedvigOrderId(),
            event.getAccountId(),
            event.getAddress(),
            event.getBank(),
            event.getCity(),
            event.getClearingHouse(),
            event.getDescriptor(),
            event.getDirectDebitMandate(),
            event.getLastDigits(),
            event.getName(),
            event.getPersonId(),
            event.getZipCode());

    commandGateway.sendAndWait(command);
  }
}
