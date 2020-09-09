package com.hedvig.paymentservice.domain.adyenTransaction

import com.adyen.model.checkout.PaymentsResponse
import com.hedvig.paymentservice.domain.adyenTransaction.commands.InitiateAdyenTransactionCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceiveAuthorisationAdyenTransactionCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceiveCancellationResponseAdyenTransactionCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceiveCaptureFailureAdyenTransactionCommand
import com.hedvig.paymentservice.domain.adyenTransaction.enums.AdyenTransactionStatus
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionAuthorisedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionCanceledEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionCancellationResponseReceivedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionInitiatedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionPendingResponseReceivedEvent
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

/**
 * This transaction seems to be very similar to the AdyenPayoutTransaction, but at this time we dont know
 * if on adyen's side these are different kinds of transactions. So we are treating them differently in
 * order to be able to handle any edge cases in the future.
 */
@Aggregate
class AdyenTransaction() {
  @AggregateIdentifier
  lateinit var transactionId: UUID
  lateinit var memberId: String
  lateinit var recurringDetailReference: String
  lateinit var transactionStatus: AdyenTransactionStatus

  @CommandHandler
  constructor(
    cmd: InitiateAdyenTransactionCommand,
    adyenService: AdyenService
  ) : this() {
    apply(
      AdyenTransactionInitiatedEvent(
        cmd.transactionId,
        cmd.memberId,
        cmd.recurringDetailReference,
        cmd.amount
      )
    )

    try {
      val request =
        ChargeMemberWithTokenRequest(cmd.transactionId, cmd.memberId, cmd.recurringDetailReference, cmd.amount)
      val response = adyenService.chargeMemberWithToken(request)

      when (response.resultCode!!) {
        PaymentsResponse.ResultCodeEnum.AUTHORISED -> {
          apply(
            AdyenTransactionAuthorisedEvent(
              cmd.transactionId,
              cmd.memberId,
              cmd.recurringDetailReference,
              cmd.amount
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
              cmd.transactionId,
              response.resultCode.value
            )
          )
        }
        PaymentsResponse.ResultCodeEnum.CANCELLED,
        PaymentsResponse.ResultCodeEnum.ERROR,
        PaymentsResponse.ResultCodeEnum.REFUSED -> {
          apply(
            AdyenTransactionCanceledEvent(
              cmd.transactionId,
              cmd.memberId,
              cmd.recurringDetailReference,
              cmd.amount,
              response.resultCode.value
            )
          )
        }
      }
    } catch (ex: Exception) {
      apply(
        AdyenTransactionCanceledEvent(
          cmd.transactionId,
          cmd.memberId,
          cmd.recurringDetailReference,
          cmd.amount,
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
  fun on(e: AdyenTransactionAuthorisedEvent) {
    transactionStatus = AdyenTransactionStatus.AUTHORISED
  }

  @EventSourcingHandler
  fun on(e: AdyenTransactionPendingResponseReceivedEvent) {
    transactionStatus = AdyenTransactionStatus.PENDING
  }

  @EventSourcingHandler
  fun on(e: AdyenTransactionCanceledEvent) {
    transactionStatus = AdyenTransactionStatus.CANCELLED
  }

  @CommandHandler
  fun handle(cmd: ReceiveCancellationResponseAdyenTransactionCommand) {
    if (transactionStatus != AdyenTransactionStatus.CANCELLED)
      apply(
        AdyenTransactionCancellationResponseReceivedEvent(
          transactionId = cmd.transactionId,
          memberId = cmd.memberId,
          reason = cmd.reason
        )
      )
  }

  @EventSourcingHandler
  fun on(e: AdyenTransactionCancellationResponseReceivedEvent) {
    transactionStatus = AdyenTransactionStatus.CANCELLED
  }

  @CommandHandler
  fun handle(cmd: ReceiveCaptureFailureAdyenTransactionCommand) {
    if (transactionStatus != AdyenTransactionStatus.CAPTURE_FAILED) {
      apply(
        CaptureFailureAdyenTransactionReceivedEvent(
          transactionId = cmd.transactionId,
          memberId = cmd.memberId
        )
      )
    }
  }

  @EventSourcingHandler
  fun on(e: CaptureFailureAdyenTransactionReceivedEvent) {
    transactionStatus = AdyenTransactionStatus.CAPTURE_FAILED
  }

  @CommandHandler
  fun handle(cmd: ReceiveAuthorisationAdyenTransactionCommand) {
    if (transactionStatus != AdyenTransactionStatus.AUTHORISED) {
      apply(
        AuthorisationAdyenTransactionReceivedEvent(
          cmd.transactionId,
          cmd.memberId
        )
      )
    }
  }

  @EventSourcingHandler
  fun on(e: AuthorisationAdyenTransactionReceivedEvent) {
    transactionStatus = AdyenTransactionStatus.AUTHORISED
  }

  companion object {
    const val EXCEPTION_MESSAGE: String = "exception"
  }
}
