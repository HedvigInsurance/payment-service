package com.hedvig.paymentservice.domain.adyenTransaction

import com.adyen.model.checkout.PaymentsResponse
import com.hedvig.paymentservice.domain.adyenTransaction.commands.InitiateAdyenTransactionCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceiveAuthorisationAdyenTransactionCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceiveCancellationResponseAdyenTransactionCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceiveCaptureFailureAdyenTransactionCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceiveAdyenTransactionUnsuccessfulRetryResponseCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceivedAdyenTransactionAutoRescueProcessEndedFromNotificationCommand
import com.hedvig.paymentservice.domain.adyenTransaction.enums.AdyenTransactionStatus
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionAuthorisedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionAutoRescueProcessEndedReceivedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionCanceledEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionCancellationResponseReceivedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionInitiatedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionPendingResponseReceivedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionRetryUnsuccessfulResponseReceivedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AuthorisationAdyenTransactionReceivedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.CaptureFailureAdyenTransactionReceivedEvent
import com.hedvig.paymentservice.services.adyen.AdyenService
import com.hedvig.paymentservice.services.adyen.dtos.ChargeMemberWithTokenRequest
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.model.AggregateIdentifier
import org.axonframework.commandhandling.model.AggregateLifecycle.apply
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.spring.stereotype.Aggregate
import java.util.UUID

@Aggregate
class AdyenTransaction() {
    @AggregateIdentifier
    lateinit var transactionId: UUID
    lateinit var memberId: String
    lateinit var recurringDetailReference: String
    lateinit var transactionStatus: AdyenTransactionStatus

    @CommandHandler
    constructor(
        command: InitiateAdyenTransactionCommand,
        adyenService: AdyenService
    ) : this() {
        apply(
            AdyenTransactionInitiatedEvent(
                command.transactionId,
                command.memberId,
                command.recurringDetailReference,
                command.amount
            )
        )

        try {
            val request =
                ChargeMemberWithTokenRequest(command.transactionId, command.memberId, command.recurringDetailReference, command.amount)
            val response = adyenService.chargeMemberWithToken(request)

            when (response.resultCode!!) {
                PaymentsResponse.ResultCodeEnum.AUTHORISED -> {
                    apply(
                        AdyenTransactionAuthorisedEvent(
                            command.transactionId,
                            command.memberId,
                            command.recurringDetailReference,
                            command.amount
                        )
                    )
                }
                PaymentsResponse.ResultCodeEnum.AUTHENTICATIONFINISHED,
                PaymentsResponse.ResultCodeEnum.AUTHENTICATIONNOTREQUIRED,
                PaymentsResponse.ResultCodeEnum.CHALLENGESHOPPER,
                PaymentsResponse.ResultCodeEnum.IDENTIFYSHOPPER,
                PaymentsResponse.ResultCodeEnum.PENDING,
                PaymentsResponse.ResultCodeEnum.RECEIVED,
                PaymentsResponse.ResultCodeEnum.PARTIALLYAUTHORISED,
                PaymentsResponse.ResultCodeEnum.PRESENTTOSHOPPER,
                PaymentsResponse.ResultCodeEnum.REDIRECTSHOPPER,
                PaymentsResponse.ResultCodeEnum.UNKNOWN -> {
                    apply(
                        AdyenTransactionPendingResponseReceivedEvent(
                            command.transactionId,
                            response.resultCode.value
                        )
                    )
                }
                PaymentsResponse.ResultCodeEnum.CANCELLED,
                PaymentsResponse.ResultCodeEnum.ERROR,
                PaymentsResponse.ResultCodeEnum.REFUSED -> {
                    apply(
                        AdyenTransactionCanceledEvent(
                            command.transactionId,
                            command.memberId,
                            command.recurringDetailReference,
                            command.amount,
                            response.resultCode.value
                        )
                    )
                }
            }
        } catch (ex: Exception) {
            apply(
                AdyenTransactionCanceledEvent(
                    command.transactionId,
                    command.memberId,
                    command.recurringDetailReference,
                    command.amount,
                    ex.message ?: EXCEPTION_MESSAGE
                )
            )
        }
    }

    @EventSourcingHandler
    fun on(e: AdyenTransactionInitiatedEvent) {
        transactionId = e.transactionId
        memberId = e.memberId
        recurringDetailReference = e.recurringDetailReference
        transactionStatus = AdyenTransactionStatus.INITIATED
    }

    @EventSourcingHandler
    fun on(event: AdyenTransactionAuthorisedEvent) {
        transactionStatus = AdyenTransactionStatus.AUTHORISED
    }

    @EventSourcingHandler
    fun on(event: AdyenTransactionPendingResponseReceivedEvent) {
        transactionStatus = AdyenTransactionStatus.PENDING
    }

    @EventSourcingHandler
    fun on(event: AdyenTransactionCanceledEvent) {
        transactionStatus = AdyenTransactionStatus.CANCELLED
    }

    @CommandHandler
    fun handle(command: ReceiveAdyenTransactionUnsuccessfulRetryResponseCommand) {
        apply(
            AdyenTransactionRetryUnsuccessfulResponseReceivedEvent(
                transactionId = command.transactionId,
                memberId = command.memberId,
                reason = command.reason,
                rescueReference = command.rescueReference,
                orderAttemptNumber = command.orderAttemptNumber
            )
        )
    }

    @CommandHandler
    fun handle(command: ReceivedAdyenTransactionAutoRescueProcessEndedFromNotificationCommand) {
        apply(
            AdyenTransactionAutoRescueProcessEndedReceivedEvent(
                transactionId = command.transactionId,
                memberId = command.memberId,
                amount = command.amount,
                reason = command.reason,
                rescueReference = command.rescueReference,
                retryWasSuccessful = command.retryWasSuccessful
            )
        )
    }

    @CommandHandler
    fun handle(command: ReceiveCancellationResponseAdyenTransactionCommand) {
        if (transactionStatus != AdyenTransactionStatus.CANCELLED) {
            apply(
                AdyenTransactionCancellationResponseReceivedEvent(
                    transactionId = command.transactionId,
                    memberId = command.memberId,
                    reason = command.reason
                )
            )
        }
    }

    @EventSourcingHandler
    fun on(event: AdyenTransactionCancellationResponseReceivedEvent) {
        transactionStatus = AdyenTransactionStatus.CANCELLED
    }

    @CommandHandler
    fun handle(command: ReceiveCaptureFailureAdyenTransactionCommand) {
        if (transactionStatus != AdyenTransactionStatus.CAPTURE_FAILED) {
            apply(
                CaptureFailureAdyenTransactionReceivedEvent(
                    transactionId = command.transactionId,
                    memberId = command.memberId
                )
            )
        }
    }

    @EventSourcingHandler
    fun on(event: CaptureFailureAdyenTransactionReceivedEvent) {
        transactionStatus = AdyenTransactionStatus.CAPTURE_FAILED
    }

    @CommandHandler
    fun handle(command: ReceiveAuthorisationAdyenTransactionCommand) {
        if (transactionStatus != AdyenTransactionStatus.AUTHORISED) {
            apply(
                AuthorisationAdyenTransactionReceivedEvent(
                    command.transactionId,
                    command.memberId
                )
            )
        }
    }

    @EventSourcingHandler
    fun on(event: AuthorisationAdyenTransactionReceivedEvent) {
        transactionStatus = AdyenTransactionStatus.AUTHORISED
    }

    companion object {
        const val EXCEPTION_MESSAGE: String = "exception"
    }
}
