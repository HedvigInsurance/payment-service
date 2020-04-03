package com.hedvig.paymentservice.query.tokenRegistration

import com.hedvig.paymentservice.domain.tokenRegistration.enums.TokenRegistrationStatus
import com.hedvig.paymentservice.domain.tokenRegistration.events.PendingTokenRegistrationCreatedEvent
import com.hedvig.paymentservice.domain.tokenRegistration.events.PendingTokenRegistrationUpdatedEvent
import com.hedvig.paymentservice.domain.tokenRegistration.events.TokenRegistrationAuthorisedEvent
import com.hedvig.paymentservice.domain.tokenRegistration.events.TokenRegistrationCanceledEvent
import com.hedvig.paymentservice.query.tokenRegistration.entities.TokenRegistration
import com.hedvig.paymentservice.query.tokenRegistration.entities.TokenRegistrationRepository
import org.axonframework.eventhandling.EventHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class TokenRegistrationEventListener(
  val adyenTokenRepository: TokenRegistrationRepository
) {
  @EventHandler
  fun on(e: TokenRegistrationAuthorisedEvent) {
    val tokenRegistration = TokenRegistration()

    tokenRegistration.tokenRegistrationId = e.adyenTokenId
    tokenRegistration.memberId = e.memberId
    tokenRegistration.recurringDetailReference = e.adyenPaymentsResponse.getRecurringDetailReference()
    tokenRegistration.tokenStatus = TokenRegistrationStatus.AUTHORISED

    adyenTokenRepository.save(tokenRegistration)
  }

  @EventHandler
  fun on(e: PendingTokenRegistrationCreatedEvent) {
    val tokenRegistration = TokenRegistration()

    tokenRegistration.tokenRegistrationId = e.adyenTokenId
    tokenRegistration.memberId = e.memberId
    tokenRegistration.recurringDetailReference = e.adyenPaymentsResponse.getRecurringDetailReference()
    tokenRegistration.tokenStatus = TokenRegistrationStatus.PENDING

    adyenTokenRepository.save(tokenRegistration)
  }

  @EventHandler
  fun on(e: PendingTokenRegistrationUpdatedEvent) {
    val tokenRegistration = TokenRegistration()

    tokenRegistration.tokenRegistrationId = e.adyenTokenId
    tokenRegistration.memberId = e.memberId
    tokenRegistration.recurringDetailReference = e.adyenPaymentsResponse.getRecurringDetailReference()
    tokenRegistration.tokenStatus = TokenRegistrationStatus.PENDING

    adyenTokenRepository.save(tokenRegistration)
  }

  @EventHandler
  fun on(e: TokenRegistrationCanceledEvent) {
    val tokenRegistration = TokenRegistration()

    tokenRegistration.tokenRegistrationId = e.adyenTokenId
    tokenRegistration.memberId = e.memberId
    tokenRegistration.recurringDetailReference = e.adyenPaymentsResponse.getRecurringDetailReference()
    tokenRegistration.tokenStatus = TokenRegistrationStatus.CANCELLED

    adyenTokenRepository.save(tokenRegistration)
  }

  companion object {
    val logger = LoggerFactory.getLogger(this::class.java)
  }
}
