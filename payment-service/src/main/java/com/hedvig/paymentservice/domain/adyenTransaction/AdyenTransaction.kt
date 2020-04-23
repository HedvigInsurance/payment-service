package com.hedvig.paymentservice.domain.adyenTransaction

import com.hedvig.paymentservice.domain.adyenTransaction.commands.InitiateAdyenTransactionCommand
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionInitiatedEvent
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
  }
}
