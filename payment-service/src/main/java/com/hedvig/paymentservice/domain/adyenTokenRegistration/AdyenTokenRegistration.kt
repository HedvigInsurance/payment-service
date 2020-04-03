package com.hedvig.paymentservice.domain.adyenTokenRegistration

import com.hedvig.paymentservice.domain.adyenTokenRegistration.commands.AuthorisedAdyenTokenRegistrationCommand
import com.hedvig.paymentservice.domain.adyenTokenRegistration.commands.CancelAdyenTokenRegistrationCommand
import com.hedvig.paymentservice.domain.adyenTokenRegistration.commands.CreateAuthorisedAdyenTokenRegistrationCommand
import com.hedvig.paymentservice.domain.adyenTokenRegistration.commands.CreatePendingAdyenTokenRegistrationCommand
import com.hedvig.paymentservice.domain.adyenTokenRegistration.commands.UpdatePendingAdyenTokenRegistrationCommand
import com.hedvig.paymentservice.domain.adyenTokenRegistration.enums.AdyenTokenRegistrationStatus
import com.hedvig.paymentservice.domain.adyenTokenRegistration.events.AdyenTokenAuthorizedEvent
import com.hedvig.paymentservice.domain.adyenTokenRegistration.events.AdyenTokenRegistrationAuthorisedEvent
import com.hedvig.paymentservice.domain.adyenTokenRegistration.events.AdyenTokenRegistrationCanceledEvent
import com.hedvig.paymentservice.domain.adyenTokenRegistration.events.PendingAdyenTokenRegistrationCreatedEvent
import com.hedvig.paymentservice.domain.adyenTokenRegistration.events.PendingAdyenTokenRegistrationUpdatedEvent
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.model.AggregateIdentifier
import org.axonframework.commandhandling.model.AggregateLifecycle.apply
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.spring.stereotype.Aggregate
import java.util.UUID

@Aggregate
class AdyenTokenRegistration() {

  @AggregateIdentifier
  lateinit var adyenTokenRegistrationId: UUID
  lateinit var memberId: String
  lateinit var adyenTokenRegistrationStatus: AdyenTokenRegistrationStatus
  var recurringDetailReference: String? = null

  @CommandHandler
  constructor(cmd: CreateAuthorisedAdyenTokenRegistrationCommand) : this() {
    apply(
      AdyenTokenRegistrationAuthorisedEvent(
        cmd.tokenRegistrationId,
        cmd.memberId,
        cmd.adyenPaymentsResponse
      )
    )
  }

  @CommandHandler
  constructor(cmd: CreatePendingAdyenTokenRegistrationCommand) : this() {
    apply(
      PendingAdyenTokenRegistrationCreatedEvent(
        cmd.tokenRegistrationId,
        cmd.memberId,
        cmd.adyenPaymentsResponse
      )
    )
  }

  @CommandHandler
  fun handle(cmd: AuthorisedAdyenTokenRegistrationCommand) {
    apply(
      AdyenTokenRegistrationAuthorisedEvent(
        cmd.tokenRegistrationId,
        cmd.memberId,
        cmd.adyenPaymentsResponse
      )
    )
  }

  @CommandHandler
  fun handle(cmd: UpdatePendingAdyenTokenRegistrationCommand) {
    apply(
      PendingAdyenTokenRegistrationUpdatedEvent(
        cmd.tokenRegistrationId,
        cmd.memberId,
        cmd.adyenPaymentsResponse
      )
    )
  }

  @CommandHandler
  fun handle(cmd: CancelAdyenTokenRegistrationCommand) {
    apply(
      AdyenTokenRegistrationCanceledEvent(
        cmd.tokenRegistrationId,
        cmd.memberId,
        cmd.adyenPaymentsResponse
      )
    )
  }

  @EventSourcingHandler
  fun on(e: AdyenTokenRegistrationAuthorisedEvent) {
    this.adyenTokenRegistrationId = e.adyenTokenRegistrationId
    this.memberId = e.memberId
    this.recurringDetailReference = e.adyenPaymentsResponse.getRecurringDetailReference()
    this.adyenTokenRegistrationStatus = AdyenTokenRegistrationStatus.AUTHORISED
  }

  @EventSourcingHandler
  fun on(e: PendingAdyenTokenRegistrationCreatedEvent) {
    this.adyenTokenRegistrationId = e.adyenTokenRegistrationId
    this.memberId = e.memberId
    this.recurringDetailReference = e.adyenPaymentsResponse.getRecurringDetailReference()
    this.adyenTokenRegistrationStatus = AdyenTokenRegistrationStatus.PENDING
  }

  @EventSourcingHandler
  fun on(e: AdyenTokenAuthorizedEvent) {
    this.adyenTokenRegistrationId = e.adyenTokenRegistrationId
    this.memberId = e.memberId
    this.recurringDetailReference = e.adyenPaymentsResponse.getRecurringDetailReference()
    this.adyenTokenRegistrationStatus = AdyenTokenRegistrationStatus.AUTHORISED
  }

  @EventSourcingHandler
  fun on(e: PendingAdyenTokenRegistrationUpdatedEvent) {
    this.adyenTokenRegistrationId = e.adyenTokenRegistrationId
    this.memberId = e.memberId
    this.recurringDetailReference = e.adyenPaymentsResponse.getRecurringDetailReference()
    this.adyenTokenRegistrationStatus = AdyenTokenRegistrationStatus.PENDING
  }

  @EventSourcingHandler
  fun on(e: AdyenTokenRegistrationCanceledEvent) {
    this.adyenTokenRegistrationId = e.adyenTokenRegistrationId
    this.memberId = e.memberId
    this.recurringDetailReference = e.adyenPaymentsResponse.getRecurringDetailReference()
    this.adyenTokenRegistrationStatus = AdyenTokenRegistrationStatus.CANCELLED
  }
}
