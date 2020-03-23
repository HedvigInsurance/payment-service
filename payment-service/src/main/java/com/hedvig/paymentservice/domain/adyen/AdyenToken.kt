package com.hedvig.paymentservice.domain.adyen

import com.adyen.model.checkout.PaymentsResponse
import com.hedvig.paymentservice.domain.adyen.commands.CreateAdyenTokenCommand
import com.hedvig.paymentservice.domain.adyen.events.AdyenTokenCreatedEvent
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.model.AggregateIdentifier
import org.axonframework.commandhandling.model.AggregateLifecycle.apply
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.spring.stereotype.Aggregate
import java.util.UUID

@Aggregate
class AdyenToken() {

  @AggregateIdentifier
  lateinit var adyenTokenId: UUID
  lateinit var memberId: String
  var recurringDetailReference: String? = null
  var tokenStatus: PaymentsResponse.ResultCodeEnum? = null

  @CommandHandler
  constructor(cmd: CreateAdyenTokenCommand) : this() {
    apply(
      AdyenTokenCreatedEvent(
        cmd.adyenTokenId,
        cmd.memberId,
        cmd.tokenizationResponse
      )
    )
  }

  //TODO: AYTHORIZE TOKEN from notification

  //TODO: CANCEL TOKEN

  @EventSourcingHandler
  fun on(e: AdyenTokenCreatedEvent) {
    this.adyenTokenId = e.adyenTokenId
    this.memberId = e.memberId
    this.recurringDetailReference = e.tokenizationResponse.getRecurringDetailReference()
    this.tokenStatus = e.tokenizationResponse.getTokenStatus()
  }

  //CHARGE USE recurringDetailReference

}
