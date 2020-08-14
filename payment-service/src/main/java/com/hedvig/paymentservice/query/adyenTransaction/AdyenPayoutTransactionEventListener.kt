package com.hedvig.paymentservice.query.adyenTransaction

import com.hedvig.paymentservice.domain.adyenTransaction.enums.AdyenPayoutTransactionStatus
import com.hedvig.paymentservice.domain.adyenTransaction.enums.AdyenTransactionStatus
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenPayoutTransactionAuthorisedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenPayoutTransactionCanceledEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenPayoutTransactionConfirmedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenPayoutTransactionInitiatedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.SuccessfulAdyenPayoutTransactionReceivedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.DeclinedAdyenPayoutTransactionReceivedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.ExpiredAdyenPayoutTransactionReceivedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.FailedAdyenPayoutTransactionReceivedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.ReservedAdyenPayoutTransactionReceivedEvent
import com.hedvig.paymentservice.query.adyenTransaction.entities.AdyenPayoutTransaction
import com.hedvig.paymentservice.query.adyenTransaction.entities.AdyenPayoutTransactionRepository
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Component
@Transactional
class AdyenPayoutTransactionEventListener(
   val adyenPayoutTransactionRepository: AdyenPayoutTransactionRepository
) {
  @EventHandler
  fun on(e: AdyenPayoutTransactionInitiatedEvent) {
    adyenPayoutTransactionRepository.save(
      AdyenPayoutTransaction(
        e.transactionId,
        e.memberId,
        e.shopperReference,
        e.amount.number.numberValueExact(BigDecimal::class.java),
        e.amount.currency.currencyCode,
        AdyenPayoutTransactionStatus.INITIATED
      )
    )
  }

  @EventHandler
  fun on(e: AdyenPayoutTransactionAuthorisedEvent) {
    val adyenTransaction = adyenPayoutTransactionRepository.findById(e.transactionId).orElseThrow()

    adyenTransaction.transactionStatus = AdyenPayoutTransactionStatus.AUTHORISED
    adyenTransaction.reason = null

    adyenPayoutTransactionRepository.save(adyenTransaction)
  }

  @EventHandler
  fun on(e: AdyenPayoutTransactionConfirmedEvent) {
    val adyenTransaction = adyenPayoutTransactionRepository.findById(e.transactionId).orElseThrow()

    adyenTransaction.transactionStatus = AdyenPayoutTransactionStatus.AUTHORISED_AND_CONFIRMED
    adyenTransaction.reason = null

    adyenPayoutTransactionRepository.save(adyenTransaction)
  }

  @EventHandler
  fun on(e: AdyenPayoutTransactionCanceledEvent) {
    val adyenTransaction = adyenPayoutTransactionRepository.findById(e.transactionId).orElseThrow()

    adyenTransaction.transactionStatus = AdyenPayoutTransactionStatus.CANCELLED
    adyenTransaction.reason = e.reason

    adyenPayoutTransactionRepository.save(adyenTransaction)
  }

  @EventHandler
  fun on(e: SuccessfulAdyenPayoutTransactionReceivedEvent) {
    val adyenTransaction = adyenPayoutTransactionRepository.findById(e.transactionId).orElseThrow()

    adyenTransaction.transactionStatus = AdyenPayoutTransactionStatus.SUCCESSFUL
    adyenTransaction.reason = null

    adyenPayoutTransactionRepository.save(adyenTransaction)
  }

  @EventHandler
  fun on(e: FailedAdyenPayoutTransactionReceivedEvent) {
    val adyenTransaction = adyenPayoutTransactionRepository.findById(e.transactionId).orElseThrow()

    adyenTransaction.transactionStatus = AdyenPayoutTransactionStatus.SUCCESSFUL
    adyenTransaction.reason = e.reason ?: "Failed"

    adyenPayoutTransactionRepository.save(adyenTransaction)
  }

  @EventHandler
  fun on(e: DeclinedAdyenPayoutTransactionReceivedEvent) {
    val adyenTransaction = adyenPayoutTransactionRepository.findById(e.transactionId).orElseThrow()

    adyenTransaction.transactionStatus = AdyenPayoutTransactionStatus.SUCCESSFUL
    adyenTransaction.reason = e.reason ?: "Declined"

    adyenPayoutTransactionRepository.save(adyenTransaction)
  }

  @EventHandler
  fun on(e: ExpiredAdyenPayoutTransactionReceivedEvent) {
    val adyenTransaction = adyenPayoutTransactionRepository.findById(e.transactionId).orElseThrow()

    adyenTransaction.transactionStatus = AdyenPayoutTransactionStatus.EXPIRED
    adyenTransaction.reason = e.reason ?: "Expired"

    adyenPayoutTransactionRepository.save(adyenTransaction)
  }

  @EventHandler
  fun on(e: ReservedAdyenPayoutTransactionReceivedEvent) {
    val adyenTransaction = adyenPayoutTransactionRepository.findById(e.transactionId).orElseThrow()

    adyenTransaction.transactionStatus = AdyenPayoutTransactionStatus.RESERVED
    adyenTransaction.reason = e.reason ?: "Reserved"

    adyenPayoutTransactionRepository.save(adyenTransaction)
  }
}
