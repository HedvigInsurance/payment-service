package com.hedvig.paymentservice.domain.adyenTransaction

import com.adyen.model.checkout.PaymentsResponse
import com.hedvig.paymentservice.domain.adyenTransaction.commands.AuthoriseAdyenTransactionCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.CancelAdyenTransactionCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.InitiateAdyenTransactionCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceiveAuthorisationAdyenTransactionCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceiveCaptureFailureAdyenTransactionCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceivePendingResponseAdyenTransaction
import com.hedvig.paymentservice.domain.adyenTransaction.enums.AdyenTransactionStatus
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionAuthorisedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionCanceledEvent
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

  @CommandHandler
  fun handle(cmd: AuthoriseAdyenTransactionCommand) {
    apply(
      AdyenTransactionAuthorisedEvent(
        transactionId = cmd.transactionId,
        memberId = cmd.memberId,
        recurringDetailReference = cmd.recurringDetailReference,
        amount = cmd.amount
      )
    )
  }

  @EventSourcingHandler
  fun on(e: AdyenTransactionAuthorisedEvent) {
    transactionStatus = AdyenTransactionStatus.AUTHORISED
  }

  @CommandHandler
  fun handle(cmd: ReceivePendingResponseAdyenTransaction) {
    apply(
      AdyenTransactionPendingResponseReceivedEvent(
        cmd.transactionId,
        cmd.reason
      )
    )
  }

  @EventSourcingHandler
  fun on(e: AdyenTransactionPendingResponseReceivedEvent) {
    transactionStatus = AdyenTransactionStatus.PENDING
  }

  @CommandHandler
  fun handle(cmd: CancelAdyenTransactionCommand) {
    apply(
      AdyenTransactionCanceledEvent(
        transactionId = cmd.transactionId,
        memberId = cmd.reason,
        recurringDetailReference = cmd.recurringDetailReference,
        amount = cmd.amount,
        reason = cmd.reason
      )
    )
  }

  @EventSourcingHandler
  fun on(e: AdyenTransactionCanceledEvent) {
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
