package com.hedvig.paymentservice.domain.trustlyOrder.sagas;

import com.hedvig.paymentservice.domain.trustlyOrder.commands.CreateAccountCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.UpdateAccountCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.events.AccountNotificationReceivedEvent;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.model.AggregateNotFoundException;
import org.axonframework.eventhandling.saga.EndSaga;
import org.axonframework.eventhandling.saga.SagaEventHandler;
import org.axonframework.eventhandling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;


@Saga
public class AccountCreationSaga {

    @Autowired
    transient CommandGateway commandGateway;

    @StartSaga
    @SagaEventHandler(associationProperty = "accountId")
    @EndSaga
    public void on(AccountNotificationReceivedEvent event) {
        // TODO: Switch to the member aggregate for this saga
        try {
            sendAccountNotification(event);
        }catch (AggregateNotFoundException ex) {
            commandGateway.sendAndWait(new CreateAccountCommand(event.getAccountId()));
            sendAccountNotification(event);
        }
    }

    private void sendAccountNotification(AccountNotificationReceivedEvent event) {
        commandGateway.sendAndWait(new UpdateAccountCommand(
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
            event.getZipCode()
        ));
    }
}
