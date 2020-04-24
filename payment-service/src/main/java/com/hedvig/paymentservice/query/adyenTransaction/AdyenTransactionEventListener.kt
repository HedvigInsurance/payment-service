package com.hedvig.paymentservice.query.adyenTransaction

import com.hedvig.paymentservice.domain.adyenTransaction.commands.CancelAdyenTransactionCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceivePendingResponseAdyenTransaction
import com.hedvig.paymentservice.domain.adyenTransaction.enums.AdyenTransactionStatus
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionAuthorisedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionInitiatedEvent
import com.hedvig.paymentservice.query.adyenTransaction.entities.AdyenTransaction
import com.hedvig.paymentservice.query.adyenTransaction.entities.AdyenTransactionRepository
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

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
  fun on(e: CancelAdyenTransactionCommand) {
    val adyenTransaction = adyenTransactionRepository.findById(e.transactionId).orElseThrow()

    adyenTransaction.transactionStatus = AdyenTransactionStatus.CANCELLED

    adyenTransactionRepository.save(adyenTransaction)
  }
}
