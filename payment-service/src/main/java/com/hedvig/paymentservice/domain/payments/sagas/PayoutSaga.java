package com.hedvig.paymentservice.domain.payments.sagas;

import com.hedvig.paymentservice.common.UUIDGenerator;
import com.hedvig.paymentservice.domain.payments.events.PayoutCreatedEvent;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.CreatePayoutOrderCommand;
import com.hedvig.paymentservice.services.trustly.TrustlyService;
import com.hedvig.paymentservice.services.trustly.dto.PayoutRequest;
import java.util.UUID;
import lombok.val;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.saga.EndSaga;
import org.axonframework.eventhandling.saga.SagaEventHandler;
import org.axonframework.eventhandling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

@Saga
public class PayoutSaga {
  @Autowired transient CommandGateway commandGateway;
  @Autowired transient TrustlyService trustlyService;
  @Autowired transient UUIDGenerator uuidGenerator;

  @StartSaga
  @SagaEventHandler(associationProperty = "memberId")
  @EndSaga
  public void on(PayoutCreatedEvent e) {
    val hedvigOrderId =
        (UUID)
            commandGateway.sendAndWait(
                new CreatePayoutOrderCommand(
                    uuidGenerator.generateRandom(),
                    e.getTransactionId(),
                    e.getMemberId(),
                    e.getAmount(),
                    e.getTrustlyAccountId(),
                    e.getAddress(),
                    e.getCountryCode(),
                    e.getDateOfBirth(),
                    e.getFirstName(),
                    e.getLastName()));

    trustlyService.startPayoutOrder(
        new PayoutRequest(
            e.getMemberId(),
            e.getAmount(),
            e.getTrustlyAccountId(),
            e.getAddress(),
            e.getCountryCode(),
            e.getDateOfBirth(),
            e.getFirstName(),
            e.getLastName()),
        hedvigOrderId);
  }
}
