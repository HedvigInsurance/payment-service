package com.hedvig.paymentservice.query.adyen

import com.hedvig.paymentservice.domain.adyen.events.AdyenTokenCreatedEvent
import com.hedvig.paymentservice.query.adyen.entities.AdyenToken
import com.hedvig.paymentservice.query.adyen.entities.AdyenTokenRepository
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class AdyenTokenEventListener(
  val adyenTokenRepository: AdyenTokenRepository
) {
  @EventHandler
  fun on(e: AdyenTokenCreatedEvent) {
    val adyenToken = AdyenToken(
      adyenTokenId = e.adyenTokenId,
      memberId = e.memberId
    )
    adyenToken.recurringDetailReference = e.tokenizationResponse.getRecurringDetailReference()
    adyenToken.tokenStatus = e.tokenizationResponse.getTokenStatus()

    adyenTokenRepository.save(adyenToken)
  }
}
