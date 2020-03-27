package com.hedvig.paymentservice.query.adyen

import com.hedvig.paymentservice.domain.adyen.events.AdyenTokenCreatedEvent
import com.hedvig.paymentservice.query.adyen.entities.AdyenTokenRepository
import org.axonframework.eventhandling.EventHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class AdyenTokenEventListener(
  val adyenTokenRepository: AdyenTokenRepository
) {
  @EventHandler
  fun on(e: AdyenTokenCreatedEvent) {
    logger.info("Adyen Token Created $e")
/*    val adyenToken = AdyenToken(
      adyenTokenId = e.adyenTokenId,
      memberId = e.memberId
    )
    adyenToken.recurringDetailReference = e.tokenizationResponse.getRecurringDetailReference()
    adyenToken.tokenStatus = e.tokenizationResponse.getTokenStatus()

    adyenTokenRepository.save(adyenToken)*/
  }

  companion object {
    val logger = LoggerFactory.getLogger(this::class.java)
  }
}
