package com.hedvig.paymentservice.domain.payments.sagas;

import com.hedvig.paymentservice.domain.payments.events.ChargeCreatedEvent;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.CreatePaymentOrderCommand;
import com.hedvig.paymentservice.services.trustly.TrustlyService;
import com.hedvig.paymentservice.services.trustly.dto.PaymentRequest;

import java.util.UUID;
import lombok.val;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.saga.EndSaga;
import org.axonframework.eventhandling.saga.SagaEventHandler;
import org.axonframework.eventhandling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

@Saga
public class ChargeSaga {
    @Autowired
    transient CommandGateway commandGateway;
    @Autowired
    transient TrustlyService trustlyService;


    @StartSaga
    @SagaEventHandler(associationProperty = "memberId")
    @EndSaga
    public void on(ChargeCreatedEvent e) {
        val hedvigOrderId = (UUID) commandGateway.sendAndWait(new CreatePaymentOrderCommand(
            e.getTransactionId(),
            e.getMemberId(),
            e.getAmount(),
            e.getCurrency(),
            e.getAccountId()
        ));
        trustlyService.startPaymentOrder(new PaymentRequest(
            e.getMemberId(),
            e.getAmount(),
            e.getCurrency(),
            e.getAccountId(),
            e.getEmail()
        ), hedvigOrderId);
    }
}
