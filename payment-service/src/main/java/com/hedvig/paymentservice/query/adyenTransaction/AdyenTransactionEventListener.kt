package com.hedvig.paymentservice.query.adyenTransaction

import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceivePendingResponseAdyenTransaction
import com.hedvig.paymentservice.domain.adyenTransaction.enums.AdyenTransactionStatus
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionAuthorisedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionCanceledEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionInitiatedEvent
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

    adyenTransactionRepository.save(adyenTransaction)
  }

  @EventHandler
  fun on(e: ReceivePendingResponseAdyenTransaction) {
    val adyenTransaction = adyenTransactionRepository.findById(e.transactionId).orElseThrow()

    adyenTransaction.transactionStatus = AdyenTransactionStatus.PENDING

    adyenTransactionRepository.save(adyenTransaction)
  }

  @EventHandler
  fun on(e: AdyenTransactionCanceledEvent) {
    val adyenTransaction = adyenTransactionRepository.findById(e.transactionId).orElseThrow()

    adyenTransaction.transactionStatus = AdyenTransactionStatus.CANCELLED

    adyenTransactionRepository.save(adyenTransaction)
  }

  @EventHandler
  fun on(e: CaptureFailureAdyenTransactionReceivedEvent) {
    val adyenTransaction = adyenTransactionRepository.findById(e.transactionId).orElseThrow()

    adyenTransaction.transactionStatus = AdyenTransactionStatus.CAPTURE_FAILED

    adyenTransactionRepository.save(adyenTransaction)
  }

  @EventHandler
  fun on(e: AuthorisationAdyenTransactionReceivedEvent) {
    val adyenTransaction = adyenTransactionRepository.findById(e.transactionId).orElseThrow()

    adyenTransaction.transactionStatus = AdyenTransactionStatus.AUTHORISED

    adyenTransactionRepository.save(adyenTransaction)
  }
}
