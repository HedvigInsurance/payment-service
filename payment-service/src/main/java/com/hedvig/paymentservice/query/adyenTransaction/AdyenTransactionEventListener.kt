package com.hedvig.paymentservice.query.adyenTransaction

import com.hedvig.paymentservice.domain.adyenTransaction.enums.AdyenTransactionStatus
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionAuthorisedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionAutoRescueProcessStartedEvent
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
    fun on(event: AdyenTransactionInitiatedEvent) {
        adyenTransactionRepository.save(
            AdyenTransaction(
                event.transactionId,
                event.memberId,
                event.recurringDetailReference,
                event.amount.number.numberValueExact(BigDecimal::class.java),
                event.amount.currency.currencyCode,
                AdyenTransactionStatus.INITIATED
            )
        )
    }

    @EventHandler
    fun on(event: AdyenTransactionAuthorisedEvent) {
        val adyenTransaction = adyenTransactionRepository.findById(event.transactionId).orElseThrow()

        adyenTransaction.transactionStatus = AdyenTransactionStatus.AUTHORISED
        adyenTransaction.reason = null

        adyenTransactionRepository.save(adyenTransaction)
    }

    @EventHandler
    fun on(event: AdyenTransactionPendingResponseReceivedEvent) {
        val adyenTransaction = adyenTransactionRepository.findById(event.transactionId).orElseThrow()

        adyenTransaction.transactionStatus = AdyenTransactionStatus.PENDING
        adyenTransaction.reason = event.reason

        adyenTransactionRepository.save(adyenTransaction)
    }

    @EventHandler
    fun on(event: AdyenTransactionCanceledEvent) {
        val adyenTransaction = adyenTransactionRepository.findById(event.transactionId).orElseThrow()

        adyenTransaction.transactionStatus = AdyenTransactionStatus.CANCELLED
        adyenTransaction.reason = event.reason

        adyenTransactionRepository.save(adyenTransaction)
    }

    @EventHandler
    fun on(event: AdyenTransactionAutoRescueProcessStartedEvent) {
        val adyenTransaction = adyenTransactionRepository.findById(event.transactionId).orElseThrow()

        adyenTransaction.transactionStatus = AdyenTransactionStatus.RESCUING
        adyenTransaction.reason = event.reason
        adyenTransaction.rescueReference = event.rescueReference

        adyenTransactionRepository.save(adyenTransaction)
    }

    @EventHandler
    fun on(event: AdyenTransactionCancellationResponseReceivedEvent) {
        val adyenTransaction = adyenTransactionRepository.findById(event.transactionId).orElseThrow()

        adyenTransaction.transactionStatus = AdyenTransactionStatus.CANCELLED
        adyenTransaction.reason = event.reason

        adyenTransactionRepository.save(adyenTransaction)
    }

    @EventHandler
    fun on(event: CaptureFailureAdyenTransactionReceivedEvent) {
        val adyenTransaction = adyenTransactionRepository.findById(event.transactionId).orElseThrow()

        adyenTransaction.transactionStatus = AdyenTransactionStatus.CAPTURE_FAILED
        adyenTransaction.reason = "Capture failure"

        adyenTransactionRepository.save(adyenTransaction)
    }

    @EventHandler
    fun on(event: AuthorisationAdyenTransactionReceivedEvent) {
        val adyenTransaction = adyenTransactionRepository.findById(event.transactionId).orElseThrow()

        adyenTransaction.transactionStatus = AdyenTransactionStatus.AUTHORISED
        adyenTransaction.reason = null

        adyenTransactionRepository.save(adyenTransaction)
    }
}
