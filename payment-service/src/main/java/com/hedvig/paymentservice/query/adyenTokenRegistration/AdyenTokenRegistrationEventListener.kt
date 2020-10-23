package com.hedvig.paymentservice.query.adyenTokenRegistration

import com.hedvig.paymentservice.domain.adyenTokenRegistration.enums.AdyenTokenRegistrationStatus
import com.hedvig.paymentservice.domain.adyenTokenRegistration.events.AdyenTokenRegistrationAuthorisedEvent
import com.hedvig.paymentservice.domain.adyenTokenRegistration.events.AdyenTokenRegistrationCanceledEvent
import com.hedvig.paymentservice.domain.adyenTokenRegistration.events.PendingAdyenTokenRegistrationCreatedEvent
import com.hedvig.paymentservice.domain.adyenTokenRegistration.events.PendingAdyenTokenRegistrationUpdatedEvent
import com.hedvig.paymentservice.query.adyenTokenRegistration.entities.AdyenTokenRegistration
import com.hedvig.paymentservice.query.adyenTokenRegistration.entities.AdyenTokenRegistrationRepository
import com.hedvig.paymentservice.query.member.entities.MemberRepository
import org.axonframework.eventhandling.EventHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class AdyenTokenRegistrationEventListener(
  val adyenAdyenTokenRepository: AdyenTokenRegistrationRepository,
  val memberRepository: MemberRepository
) {
  @EventHandler
  fun on(e: AdyenTokenRegistrationAuthorisedEvent) {
    val tokenRegistration =
      adyenAdyenTokenRepository.findById(e.adyenTokenRegistrationId).orElse(AdyenTokenRegistration())

    tokenRegistration.adyenTokenRegistrationId = e.adyenTokenRegistrationId
    tokenRegistration.memberId = e.memberId
    tokenRegistration.recurringDetailReference = e.adyenPaymentsResponse.getRecurringDetailReference()
    tokenRegistration.tokenStatus = AdyenTokenRegistrationStatus.AUTHORISED

    adyenAdyenTokenRepository.save(tokenRegistration)

    val memberMaybe = memberRepository.findById(e.memberId)

    if (memberMaybe.isPresent) {
      val member = memberMaybe.get()
      member.adyenMerchantAccount = e.adyenMerchantAccount
      memberRepository.save(member)
    }
  }

  @EventHandler
  fun on(e: PendingAdyenTokenRegistrationCreatedEvent) {
    val tokenRegistration = AdyenTokenRegistration()

    tokenRegistration.adyenTokenRegistrationId = e.adyenTokenRegistrationId
    tokenRegistration.memberId = e.memberId
    tokenRegistration.recurringDetailReference = e.adyenPaymentsResponse.getRecurringDetailReference()
    tokenRegistration.tokenStatus = AdyenTokenRegistrationStatus.PENDING
    tokenRegistration.paymentDataFromAction = e.paymentDataFromAction

    adyenAdyenTokenRepository.save(tokenRegistration)

    val memberMaybe = memberRepository.findById(e.memberId)

    if (memberMaybe.isPresent) {
      val member = memberMaybe.get()
      member.adyenMerchantAccount = e.adyenMerchantAccount
      memberRepository.save(member)
    }
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
