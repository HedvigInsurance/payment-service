package com.hedvig.paymentservice.query.adyenTransaction

import com.hedvig.paymentservice.domain.adyenTransaction.enums.AdyenTransactionStatus
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionAuthorisedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionCanceledEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionCancellationResponseReceivedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionInitiatedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionPendingResponseReceivedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AuthorisationAdyenTransactionReceivedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.CaptureFailureAdyenTransactionReceivedEvent
import com.hedvig.paymentservice.query.adyenTransaction.entities.AdyenTransaction
import com.hedvig.paymentservice.query.adyenTransaction.entities.AdyenTransactionRepository
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Component
@Transactional
class AdyenTransactionEventListener(
  val adyenTransactionRepository: AdyenTransactionRepository
) {
  @EventHandler
  fun on(e: AdyenTransactionInitiatedEvent) {
    adyenTransactionRepository.save(
      AdyenTransaction(
        e.transactionId,
        e.memberId,
        e.recurringDetailReference,
        e.amount.number.numberValueExact(BigDecimal::class.java),
        e.amount.currency.currencyCode,
        AdyenTransactionStatus.INITIATED
      )
    )
  }

  @EventHandler
  fun on(e: AdyenTransactionAuthorisedEvent) {
    val adyenTransaction = adyenTransactionRepository.findById(e.transactionId).orElseThrow()

    adyenTransaction.transactionStatus = AdyenTransactionStatus.AUTHORISED
    adyenTransaction.reason = null

    adyenTransactionRepository.save(adyenTransaction)
  }

  @EventHandler
  fun on(e: AdyenTransactionPendingResponseReceivedEvent) {
    val adyenTransaction = adyenTransactionRepository.findById(e.transactionId).orElseThrow()

    adyenTransaction.transactionStatus = AdyenTransactionStatus.PENDING
    adyenTransaction.reason = e.reason

    adyenTransactionRepository.save(adyenTransaction)
  }

  @EventHandler
  fun on(e: AdyenTransactionCanceledEvent) {
    val adyenTransaction = adyenTransactionRepository.findById(e.transactionId).orElseThrow()

    adyenTransaction.transactionStatus = AdyenTransactionStatus.CANCELLED
    adyenTransaction.reason = e.reason

    adyenTransactionRepository.save(adyenTransaction)
  }

  @EventHandler
  fun on(e: AdyenTransactionCancellationResponseReceivedEvent) {
    val adyenTransaction = adyenTransactionRepository.findById(e.transactionId).orElseThrow()

    adyenTransaction.transactionStatus = AdyenTransactionStatus.CANCELLED
    adyenTransaction.reason = e.reason

    adyenTransactionRepository.save(adyenTransaction)
  }

  @EventHandler
  fun on(e: CaptureFailureAdyenTransactionReceivedEvent) {
    val adyenTransaction = adyenTransactionRepository.findById(e.transactionId).orElseThrow()

    adyenTransaction.transactionStatus = AdyenTransactionStatus.CAPTURE_FAILED
    adyenTransaction.reason = "Capture failure"

    adyenTransactionRepository.save(adyenTransaction)
  }

  @EventHandler
  fun on(e: AuthorisationAdyenTransactionReceivedEvent) {
    val adyenTransaction = adyenTransactionRepository.findById(e.transactionId).orElseThrow()

    adyenTransaction.transactionStatus = AdyenTransactionStatus.AUTHORISED
    adyenTransaction.reason = null

    adyenTransactionRepository.save(adyenTransaction)
  }
}
