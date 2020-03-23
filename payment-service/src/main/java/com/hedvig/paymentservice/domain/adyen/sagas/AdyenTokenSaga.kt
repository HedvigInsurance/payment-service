package com.hedvig.paymentservice.domain.adyen.sagas

import com.hedvig.paymentservice.domain.adyen.events.AdyenTokenCreatedEvent
import com.hedvig.paymentservice.domain.payments.commands.UpdateAdyenAccountCommand
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.saga.EndSaga
import org.axonframework.eventhandling.saga.SagaEventHandler
import org.axonframework.eventhandling.saga.StartSaga
import org.axonframework.spring.stereotype.Saga
import org.springframework.beans.factory.annotation.Autowired

@Saga
class AdyenTokenSaga {

  @Autowired
  @Transient
  private lateinit var commandGateway: CommandGateway

  @StartSaga
  @SagaEventHandler(associationProperty = ADYEN_TOKEN_ID)
  @EndSaga
  fun on(e: AdyenTokenCreatedEvent) {
    commandGateway.sendAndWait<Void>(
      UpdateAdyenAccountCommand(
        e.memberId,
        e.adyenTokenId.toString(),
        e.tokenizationResponse.getRecurringDetailReference(),
        e.tokenizationResponse.getTokenStatus()
      )
    )
  }

  companion object {
    const val ADYEN_TOKEN_ID: String = "adyenTokenId"
  }
}
