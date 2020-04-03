package com.hedvig.paymentservice.domain.adyenTokenRegistration.sagas

import com.hedvig.paymentservice.domain.adyenTokenRegistration.enums.AdyenTokenRegistrationStatus
import com.hedvig.paymentservice.domain.adyenTokenRegistration.events.AdyenTokenRegistrationAuthorisedEvent
import com.hedvig.paymentservice.domain.payments.commands.UpdateAdyenAccountCommand
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.saga.EndSaga
import org.axonframework.eventhandling.saga.SagaEventHandler
import org.axonframework.eventhandling.saga.StartSaga
import org.axonframework.spring.stereotype.Saga
import org.springframework.beans.factory.annotation.Autowired

@Saga
class AdyenTokenRegistrationSaga {

  @Autowired
  @Transient
  private lateinit var commandGateway: CommandGateway

  @StartSaga
  @SagaEventHandler(associationProperty = ADYEN_TOKEN_ID)
  @EndSaga
  fun on(e: AdyenTokenRegistrationAuthorisedEvent) {
    commandGateway.sendAndWait<Void>(
      UpdateAdyenAccountCommand(
        e.memberId,
        e.adyenPaymentsResponse.getRecurringDetailReference()!!,
        AdyenTokenRegistrationStatus.AUTHORISED
      )
    )
  }

  companion object {
    const val ADYEN_TOKEN_ID: String = "adyenTokenId"
  }
}
