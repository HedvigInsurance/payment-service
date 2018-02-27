package com.hedvig.paymentservice.domain.trustlyOrder.sagas;

import com.hedvig.paymentservice.domain.trustlyOrder.commands.AccountNotificationReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.CreateAccountCommand;
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
        try {
            sendAccountNotification(event);
        }catch (AggregateNotFoundException ex) {
            commandGateway.sendAndWait(new CreateAccountCommand(event.getAccountId()));
            sendAccountNotification(event);
        }
    }

    private void sendAccountNotification(AccountNotificationReceivedEvent event) {
        commandGateway.sendAndWait(new AccountNotificationReceivedCommand(event.getAccountId(), event));
    }
}