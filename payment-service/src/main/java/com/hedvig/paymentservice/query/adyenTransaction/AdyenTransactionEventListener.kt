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
import org.slf4j.LoggerFactory
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
        logger.info("AdyenTransactionEventListener: Handling event AdyenTransactionInitiatedEvent (memberId=${event.memberId}, transactionId=${event.transactionId})")
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
        logger.info("AdyenTransactionEventListener: Handling event AdyenTransactionAuthorisedEvent (memberId=${event.memberId}, transactionId=${event.transactionId})")

        val adyenTransaction = adyenTransactionRepository.findById(event.transactionId).orElseThrow()

        adyenTransaction.transactionStatus = AdyenTransactionStatus.AUTHORISED
        adyenTransaction.reason = null

        adyenTransactionRepository.save(adyenTransaction)
    }

    @EventHandler
    fun on(event: AdyenTransactionPendingResponseReceivedEvent) {
        logger.info("AdyenTransactionEventListener: Handling event AdyenTransactionPendingResponseReceivedEvent (transactionId=${event.transactionId})")

        val adyenTransaction = adyenTransactionRepository.findById(event.transactionId).orElseThrow()

        adyenTransaction.transactionStatus = AdyenTransactionStatus.PENDING
        adyenTransaction.reason = event.reason

        adyenTransactionRepository.save(adyenTransaction)
    }

    @EventHandler
    fun on(event: AdyenTransactionCanceledEvent) {
        logger.info("AdyenTransactionEventListener: Handling event AdyenTransactionCanceledEvent (memberId=${event.memberId}, transactionId=${event.transactionId})")

        val adyenTransaction = adyenTransactionRepository.findById(event.transactionId).orElseThrow()

        adyenTransaction.transactionStatus = AdyenTransactionStatus.CANCELLED
        adyenTransaction.reason = event.reason

        adyenTransactionRepository.save(adyenTransaction)
    }

    @EventHandler
    fun on(event: AdyenTransactionAutoRescueProcessStartedEvent) {
        logger.info("AdyenTransactionEventListener: Handling event AdyenTransactionAutoRescueProcessStartedEvent (memberId=${event.memberId}, transactionId=${event.transactionId})")

        val adyenTransaction = adyenTransactionRepository.findById(event.transactionId).orElseThrow()

        adyenTransaction.transactionStatus = AdyenTransactionStatus.RESCUING
        adyenTransaction.reason = event.reason
        adyenTransaction.rescueReference = event.rescueReference

        adyenTransactionRepository.save(adyenTransaction)
    }

    @EventHandler
    fun on(event: AdyenTransactionCancellationResponseReceivedEvent) {
        logger.info("AdyenTransactionEventListener: Handling event AdyenTransactionCancellationResponseReceivedEvent (memberId=${event.memberId}, transactionId=${event.transactionId})")

        val adyenTransaction = adyenTransactionRepository.findById(event.transactionId).orElseThrow()

        adyenTransaction.transactionStatus = AdyenTransactionStatus.CANCELLED
        adyenTransaction.reason = event.reason

        adyenTransactionRepository.save(adyenTransaction)
    }

    @EventHandler
    fun on(event: CaptureFailureAdyenTransactionReceivedEvent) {
        logger.info("AdyenTransactionEventListener: Handling event CaptureFailureAdyenTransactionReceivedEvent (memberId=${event.memberId}, transactionId=${event.transactionId})")

        val adyenTransaction = adyenTransactionRepository.findById(event.transactionId).orElseThrow()

        adyenTransaction.transactionStatus = AdyenTransactionStatus.CAPTURE_FAILED
        adyenTransaction.reason = "Capture failure"

        adyenTransactionRepository.save(adyenTransaction)
    }

    @EventHandler
    fun on(event: AuthorisationAdyenTransactionReceivedEvent) {
        logger.info("AdyenTransactionEventListener: Handling event AuthorisationAdyenTransactionReceivedEvent (memberId=${event.memberId}, transactionId=${event.transactionId})")

        val adyenTransaction = adyenTransactionRepository.findById(event.transactionId).orElseThrow()

        adyenTransaction.transactionStatus = AdyenTransactionStatus.AUTHORISED
        adyenTransaction.reason = null

        adyenTransactionRepository.save(adyenTransaction)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AdyenTransactionEventListener::class.java)
    }
}
