package com.hedvig.paymentservice.domain.tokenRegistration.sagas

import com.hedvig.paymentservice.domain.payments.commands.UpdateAdyenAccountCommand
import com.hedvig.paymentservice.domain.tokenRegistration.enums.TokenRegistrationStatus
import com.hedvig.paymentservice.domain.tokenRegistration.events.TokenRegistrationAuthorisedEvent
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.saga.EndSaga
import org.axonframework.eventhandling.saga.SagaEventHandler
import org.axonframework.eventhandling.saga.StartSaga
import org.axonframework.spring.stereotype.Saga
import org.springframework.beans.factory.annotation.Autowired

@Saga
class TokenRegistrationSaga {

  @Autowired
  @Transient
  private lateinit var commandGateway: CommandGateway

  @StartSaga
  @SagaEventHandler(associationProperty = ADYEN_TOKEN_ID)
  @EndSaga
  fun on(e: TokenRegistrationAuthorisedEvent) {
    commandGateway.sendAndWait<Void>(
      UpdateAdyenAccountCommand(
        e.memberId,
        e.adyenPaymentsResponse.getRecurringDetailReference()!!,
        TokenRegistrationStatus.AUTHORISED
      )
    )
  }

  companion object {
    const val ADYEN_TOKEN_ID: String = "adyenTokenId"
  }
}
