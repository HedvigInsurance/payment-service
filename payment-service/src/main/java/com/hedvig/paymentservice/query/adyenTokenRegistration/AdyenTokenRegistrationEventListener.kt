package com.hedvig.paymentservice.query.adyenTokenRegistration

import com.hedvig.paymentservice.domain.adyenTokenRegistration.enums.AdyenTokenRegistrationStatus
import com.hedvig.paymentservice.domain.adyenTokenRegistration.events.AdyenTokenRegistrationAuthorisedEvent
import com.hedvig.paymentservice.domain.adyenTokenRegistration.events.AdyenTokenRegistrationAuthorisedFromNotificationEvent
import com.hedvig.paymentservice.domain.adyenTokenRegistration.events.AdyenTokenRegistrationCanceledEvent
import com.hedvig.paymentservice.domain.adyenTokenRegistration.events.PendingAdyenTokenRegistrationCreatedEvent
import com.hedvig.paymentservice.domain.adyenTokenRegistration.events.PendingAdyenTokenRegistrationUpdatedEvent
import com.hedvig.paymentservice.query.adyenTokenRegistration.entities.AdyenTokenRegistration
import com.hedvig.paymentservice.query.adyenTokenRegistration.entities.AdyenTokenRegistrationRepository
import org.axonframework.eventhandling.EventHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class AdyenTokenRegistrationEventListener(
  val adyenAdyenTokenRepository: AdyenTokenRegistrationRepository
) {
  @EventHandler
  fun on(e: AdyenTokenRegistrationAuthorisedEvent) {
    val tokenRegistration =
      adyenAdyenTokenRepository.findById(e.adyenTokenRegistrationId).orElse(AdyenTokenRegistration())

    tokenRegistration.adyenTokenRegistrationId = e.adyenTokenRegistrationId
    tokenRegistration.memberId = e.memberId
    tokenRegistration.recurringDetailReference = e.adyenPaymentsResponse.getRecurringDetailReference()
    tokenRegistration.tokenStatus = AdyenTokenRegistrationStatus.AUTHORISED
    tokenRegistration.isForPayout = e.isPayoutSetup

    adyenAdyenTokenRepository.save(tokenRegistration)
  }

  @EventHandler
  fun on(e: AdyenTokenRegistrationAuthorisedFromNotificationEvent) {
    TODO("Implement")
  }

  @EventHandler
  fun on(e: PendingAdyenTokenRegistrationCreatedEvent) {
    val tokenRegistration = AdyenTokenRegistration()

    tokenRegistration.adyenTokenRegistrationId = e.adyenTokenRegistrationId
    tokenRegistration.memberId = e.memberId
    tokenRegistration.recurringDetailReference = e.adyenPaymentsResponse.getRecurringDetailReference()
    tokenRegistration.tokenStatus = AdyenTokenRegistrationStatus.PENDING
    tokenRegistration.paymentDataFromAction = e.paymentDataFromAction
    tokenRegistration.isForPayout = e.isPayoutSetup

    adyenAdyenTokenRepository.save(tokenRegistration)
  }

  @EventHandler
  fun on(e: PendingAdyenTokenRegistrationUpdatedEvent) {
    val tokenRegistration = adyenAdyenTokenRepository.findById(e.adyenTokenRegistrationId).orElseThrow()

    tokenRegistration.memberId = e.memberId
    tokenRegistration.recurringDetailReference = e.adyenPaymentsResponse.getRecurringDetailReference()
    tokenRegistration.tokenStatus = AdyenTokenRegistrationStatus.PENDING

    adyenAdyenTokenRepository.save(tokenRegistration)
  }

  @EventHandler
  fun on(e: AdyenTokenRegistrationCanceledEvent) {
    val tokenRegistration = adyenAdyenTokenRepository.findById(e.adyenTokenRegistrationId).orElseThrow()

    tokenRegistration.adyenTokenRegistrationId = e.adyenTokenRegistrationId
    tokenRegistration.memberId = e.memberId
    tokenRegistration.recurringDetailReference = e.adyenPaymentsResponse.getRecurringDetailReference()
    tokenRegistration.tokenStatus = AdyenTokenRegistrationStatus.CANCELLED

    adyenAdyenTokenRepository.save(tokenRegistration)
  }

  companion object {
    val logger = LoggerFactory.getLogger(this::class.java)
  }
}
