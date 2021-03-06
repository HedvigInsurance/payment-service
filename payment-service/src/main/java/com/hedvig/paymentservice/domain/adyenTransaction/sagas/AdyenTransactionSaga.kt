package com.hedvig.paymentservice.domain.adyenTransaction.sagas

import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionAuthorisationResponseReceivedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionAuthorisedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionCanceledEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionCancellationResponseReceivedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.CaptureFailureAdyenTransactionReceivedEvent
import com.hedvig.paymentservice.domain.payments.commands.ChargeCompletedCommand
import com.hedvig.paymentservice.domain.payments.commands.ChargeFailedCommand
import java.time.Instant
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.saga.EndSaga
import org.axonframework.eventhandling.saga.SagaEventHandler
import org.axonframework.eventhandling.saga.StartSaga
import org.axonframework.spring.stereotype.Saga
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

@Saga
class AdyenTransactionSaga {
    @Autowired
    @Transient
    private lateinit var commandGateway: CommandGateway

    /*
    * When the capture delay is set to immediate and the authorisation is successful, the funds are immediately captured.
    * Because it is immediate, Adyen only send an authorisation notification.
    * In that scenario, we can indeed consider the transaction is successful after the authorisation.
    *
    * If for some reason the capture fails, we will receive a capture_failed notification as described in Adyen's documentation.
    *
    * */
    @StartSaga
    @SagaEventHandler(associationProperty = TRANSACTION_ID)
    @EndSaga
    fun on(event: AdyenTransactionAuthorisedEvent) {
        commandGateway.sendAndWait<Void>(
            ChargeCompletedCommand(
                memberId = event.memberId,
                transactionId = event.transactionId,
                amount = event.amount,
                timestamp = Instant.now()
            )
        )
    }

    @StartSaga
    @SagaEventHandler(associationProperty = TRANSACTION_ID)
    @EndSaga
    fun on(event: AdyenTransactionCanceledEvent) {
        commandGateway.sendAndWait<Void>(
            ChargeFailedCommand(
                memberId = event.memberId,
                transactionId = event.transactionId
            )
        )
    }

    @StartSaga
    @SagaEventHandler(associationProperty = TRANSACTION_ID)
    @EndSaga
    fun on(event: CaptureFailureAdyenTransactionReceivedEvent) {
        commandGateway.sendAndWait<Void>(
            ChargeFailedCommand(
                memberId = event.memberId,
                transactionId = event.transactionId
            )
        )
    }

    @StartSaga
    @SagaEventHandler(associationProperty = TRANSACTION_ID)
    @EndSaga
    fun on(event: AdyenTransactionCancellationResponseReceivedEvent) {
        commandGateway.sendAndWait<Void>(
            ChargeFailedCommand(
                memberId = event.memberId,
                transactionId = event.transactionId
            )
        )
    }

    @StartSaga
    @SagaEventHandler(associationProperty = TRANSACTION_ID)
    @EndSaga
    fun on(event: AdyenTransactionAuthorisationResponseReceivedEvent) {
        commandGateway.sendAndWait<Void>(
            ChargeCompletedCommand(
                memberId = event.memberId,
                transactionId = event.transactionId,
                amount = event.amount,
                timestamp = Instant.now()
            )
        )
    }

    companion object {
        const val TRANSACTION_ID: String = "transactionId"
        val logger = LoggerFactory.getLogger(this::class.java)!!
    }
}
