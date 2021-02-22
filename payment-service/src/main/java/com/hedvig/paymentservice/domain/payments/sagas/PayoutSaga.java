package com.hedvig.paymentservice.domain.payments.sagas;

import com.hedvig.paymentservice.common.UUIDGenerator;
import com.hedvig.paymentservice.domain.adyenTransaction.commands.InitiateAdyenTransactionPayoutCommand;
import com.hedvig.paymentservice.domain.payments.events.PayoutCreatedEvent;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.CreatePayoutOrderCommand;
import com.hedvig.paymentservice.services.adyen.AdyenService;
import com.hedvig.paymentservice.services.trustly.TrustlyService;
import com.hedvig.paymentservice.services.trustly.dto.PayoutRequest;
import java.util.UUID;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.saga.EndSaga;
import org.axonframework.eventhandling.saga.SagaEventHandler;
import org.axonframework.eventhandling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

@Saga
public class PayoutSaga {
    @Autowired
    transient CommandGateway commandGateway;
    @Autowired
    transient TrustlyService trustlyService;
    @Autowired
    transient AdyenService adyenService;
    @Autowired
    transient UUIDGenerator uuidGenerator;

    @StartSaga
    @SagaEventHandler(associationProperty = "memberId")
    @EndSaga
    public void on(PayoutCreatedEvent event) {
        if (event.getTrustlyAccountId() != null) {
            final UUID hedvigOrderId = commandGateway.sendAndWait(
                new CreatePayoutOrderCommand(
                    uuidGenerator.generateRandom(),
                    event.getTransactionId(),
                    event.getMemberId(),
                    event.getAmount(),
                    event.getTrustlyAccountId(),
                    event.getAddress(),
                    event.getCountryCode(),
                    event.getDateOfBirth(),
                    event.getFirstName(),
                    event.getLastName()
                )
            );

            trustlyService.startPayoutOrder(
                new PayoutRequest(
                    event.getMemberId(),
                    event.getAmount(),
                    event.getTrustlyAccountId(),
                    event.getAddress(),
                    event.getCountryCode(),
                    event.getDateOfBirth(),
                    event.getFirstName(),
                    event.getLastName(),
                    event.getCategory(),
                    event.getCarrier()
                ),
                hedvigOrderId
            );
            return;
        }

        if (event.getAdyenShopperReference() != null) {
            commandGateway.sendAndWait(
                new InitiateAdyenTransactionPayoutCommand(
                    event.getTransactionId(),
                    event.getMemberId(),
                    event.getAdyenShopperReference(),
                    event.getAmount(),
                    event.getEmail() != null ? event.getEmail() : ""
                )
            );
            return;
        }

        throw new RuntimeException("Payout event must have either 'trustlyAccountId' or 'adyenShopperReference' [event: " + event.toString() + "]");
    }
}
