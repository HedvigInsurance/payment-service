package com.hedvig.paymentservice.domain.adyenTransaction

import com.hedvig.paymentservice.domain.adyenTransaction.commands.AuthoriseAdyenTransactionCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.CancelAdyenTransactionCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.InitiateAdyenTransactionCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceiveCaptureFailureAdyenTransactionCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceivePendingResponseAdyenTransaction
import com.hedvig.paymentservice.domain.adyenTransaction.enums.AdyenTransactionStatus
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionAuthorisedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionCanceledEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionInitiatedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionPendingResponseReceivedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.CaptureFailureAdyenTransactionReceivedEvent
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
  constructor(cmd: InitiateAdyenTransactionCommand) : this() {
    apply(
      AdyenTransactionInitiatedEvent(
        cmd.transactionId,
        cmd.memberId,
        cmd.recurringDetailReference,
        cmd.amount
      )
    )
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
    apply(
      CaptureFailureAdyenTransactionReceivedEvent(
        transactionId = cmd.transactionId,
        memberId = cmd.memberId
      )
    )
  }

  @EventSourcingHandler
  fun on(e: CaptureFailureAdyenTransactionReceivedEvent) {
    transactionStatus = AdyenTransactionStatus.CAPTURE_FAILED
  }
}
