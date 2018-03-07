package com.hedvig.paymentservice.domain.trustlyOrder.sagas;

import com.hedvig.paymentservice.domain.payments.commands.ChargeCompletedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.events.CreditNotificationReceivedEvent;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.saga.EndSaga;
import org.axonframework.eventhandling.saga.SagaEventHandler;
import org.axonframework.eventhandling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

@Saga
public class CreditSaga {
    @Autowired
    transient CommandGateway commandGateway;

    @StartSaga
    @SagaEventHandler(associationProperty = "hedvigOrderId")
    @EndSaga
    public void on(CreditNotificationReceivedEvent e) {
        commandGateway.sendAndWait(new ChargeCompletedCommand(
            e.getMemberId(),
            e.getTransactionId(),
            e.getAmount(),
            e.getTimestamp()
        ));
    }
}
