package com.hedvig.paymentservice.domain.tokenRegistration

import com.hedvig.paymentservice.domain.tokenRegistration.commands.AuthorisedTokenRegistrationCommand
import com.hedvig.paymentservice.domain.tokenRegistration.commands.CancelTokenRegistrationCommand
import com.hedvig.paymentservice.domain.tokenRegistration.commands.CreateAuthorisedTokenRegistrationCommand
import com.hedvig.paymentservice.domain.tokenRegistration.commands.CreatePendingTokenRegistrationCommand
import com.hedvig.paymentservice.domain.tokenRegistration.commands.UpdatePendingTokenRegistrationCommand
import com.hedvig.paymentservice.domain.tokenRegistration.enums.TokenRegistrationStatus
import com.hedvig.paymentservice.domain.tokenRegistration.events.AdyenTokenAuthorizedEvent
import com.hedvig.paymentservice.domain.tokenRegistration.events.PendingTokenRegistrationCreatedEvent
import com.hedvig.paymentservice.domain.tokenRegistration.events.PendingTokenRegistrationUpdatedEvent
import com.hedvig.paymentservice.domain.tokenRegistration.events.TokenRegistrationAuthorisedEvent
import com.hedvig.paymentservice.domain.tokenRegistration.events.TokenRegistrationCanceledEvent
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.model.AggregateIdentifier
import org.axonframework.commandhandling.model.AggregateLifecycle.apply
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.spring.stereotype.Aggregate
import java.util.UUID

@Aggregate
class TokenRegistration() {

  @AggregateIdentifier
  lateinit var tokenRegistrationId: UUID
  lateinit var memberId: String
  lateinit var tokenRegistrationStatus: TokenRegistrationStatus
  var recurringDetailReference: String? = null

  @CommandHandler
  constructor(cmd: CreateAuthorisedTokenRegistrationCommand) : this() {
    apply(
      TokenRegistrationAuthorisedEvent(
        cmd.tokenRegistrationId,
        cmd.memberId,
        cmd.adyenPaymentsResponse
      )
    )
  }

  @CommandHandler
  constructor(cmd: CreatePendingTokenRegistrationCommand) : this() {
    apply(
      PendingTokenRegistrationCreatedEvent(
        cmd.tokenRegistrationId,
        cmd.memberId,
        cmd.adyenPaymentsResponse
      )
    )
  }

  @CommandHandler
  fun handle(cmd: AuthorisedTokenRegistrationCommand) {
    apply(
      TokenRegistrationAuthorisedEvent(
        cmd.tokenRegistrationId,
        cmd.memberId,
        cmd.adyenPaymentsResponse
      )
    )
  }

  @CommandHandler
  fun handle(cmd: UpdatePendingTokenRegistrationCommand) {
    apply(
      PendingTokenRegistrationUpdatedEvent(
        cmd.tokenRegistrationId,
        cmd.memberId,
        cmd.adyenPaymentsResponse
      )
    )
  }

  @CommandHandler
  fun handle(cmd: CancelTokenRegistrationCommand) {
    apply(
      TokenRegistrationCanceledEvent(
        cmd.tokenRegistrationId,
        cmd.memberId,
        cmd.adyenPaymentsResponse
      )
    )
  }

  @EventSourcingHandler
  fun on(e: TokenRegistrationAuthorisedEvent) {
    this.tokenRegistrationId = e.adyenTokenId
    this.memberId = e.memberId
    this.recurringDetailReference = e.adyenPaymentsResponse.getRecurringDetailReference()
    this.tokenRegistrationStatus = TokenRegistrationStatus.AUTHORISED
  }

  @EventSourcingHandler
  fun on(e: PendingTokenRegistrationCreatedEvent) {
    this.tokenRegistrationId = e.adyenTokenId
    this.memberId = e.memberId
    this.recurringDetailReference = e.adyenPaymentsResponse.getRecurringDetailReference()
    this.tokenRegistrationStatus = TokenRegistrationStatus.PENDING
  }

  @EventSourcingHandler
  fun on(e: AdyenTokenAuthorizedEvent) {
    this.tokenRegistrationId = e.adyenTokenId
    this.memberId = e.memberId
    this.recurringDetailReference = e.adyenPaymentsResponse.getRecurringDetailReference()
    this.tokenRegistrationStatus = TokenRegistrationStatus.AUTHORISED
  }

  @EventSourcingHandler
  fun on(e: PendingTokenRegistrationUpdatedEvent) {
    this.tokenRegistrationId = e.adyenTokenId
    this.memberId = e.memberId
    this.recurringDetailReference = e.adyenPaymentsResponse.getRecurringDetailReference()
    this.tokenRegistrationStatus = TokenRegistrationStatus.PENDING
  }

  @EventSourcingHandler
  fun on(e: TokenRegistrationCanceledEvent) {
    this.tokenRegistrationId = e.adyenTokenId
    this.memberId = e.memberId
    this.recurringDetailReference = e.adyenPaymentsResponse.getRecurringDetailReference()
    this.tokenRegistrationStatus = TokenRegistrationStatus.CANCELLED
  }
}
