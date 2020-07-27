package com.hedvig.paymentservice.domain.adyenTokenRegistration

import com.hedvig.paymentservice.domain.adyenTokenRegistration.commands.AuthoriseAdyenTokenRegistrationFromNotificationCommand
import com.hedvig.paymentservice.domain.adyenTokenRegistration.commands.AuthorisedAdyenTokenRegistrationCommand
import com.hedvig.paymentservice.domain.adyenTokenRegistration.commands.CancelAdyenTokenRegistrationCommand
import com.hedvig.paymentservice.domain.adyenTokenRegistration.commands.CreateAuthorisedAdyenTokenRegistrationCommand
import com.hedvig.paymentservice.domain.adyenTokenRegistration.commands.CreatePendingAdyenTokenRegistrationCommand
import com.hedvig.paymentservice.domain.adyenTokenRegistration.commands.UpdatePendingAdyenTokenRegistrationCommand
import com.hedvig.paymentservice.domain.adyenTokenRegistration.enums.AdyenTokenRegistrationStatus
import com.hedvig.paymentservice.domain.adyenTokenRegistration.events.AdyenTokenRegistrationAuthorisedEvent
import com.hedvig.paymentservice.domain.adyenTokenRegistration.events.AdyenTokenRegistrationAuthorisedFromNotificationEvent
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
  var paymentDataFromAction: String? = null
  var isForPayout: Boolean = false

  @CommandHandler
  constructor(cmd: CreateAuthorisedAdyenTokenRegistrationCommand) : this() {
    apply(
      AdyenTokenRegistrationAuthorisedEvent(
        cmd.adyenTokenRegistrationId,
        cmd.memberId,
        cmd.adyenPaymentsResponse,
        cmd.isPayoutSetup
      )
    )
  }

  @CommandHandler
  constructor(cmd: CreatePendingAdyenTokenRegistrationCommand) : this() {
    apply(
      PendingAdyenTokenRegistrationCreatedEvent(
        cmd.adyenTokenRegistrationId,
        cmd.memberId,
        cmd.adyenPaymentsResponse,
        cmd.paymentDataFromAction,
        cmd.isPayoutSetup
      )
    )
  }

  @CommandHandler
  fun handle(cmd: AuthorisedAdyenTokenRegistrationCommand) {
    apply(
      AdyenTokenRegistrationAuthorisedEvent(
        cmd.adyenTokenRegistrationId,
        cmd.memberId,
        cmd.adyenPaymentsResponse,
        false
      )
    )
  }

  @CommandHandler
  fun handle(cmd: AuthoriseAdyenTokenRegistrationFromNotificationCommand) {
    apply(
      AdyenTokenRegistrationAuthorisedFromNotificationEvent(
        cmd.adyenTokenRegistrationId,
        cmd.memberId,
        cmd.adyenNotification
      )
    )
  }

  @CommandHandler
  fun handle(cmd: UpdatePendingAdyenTokenRegistrationCommand) {
    apply(
      PendingAdyenTokenRegistrationUpdatedEvent(
        cmd.adyenTokenRegistrationId,
        cmd.memberId,
        cmd.adyenPaymentsResponse
      )
    )
  }

  @CommandHandler
  fun handle(cmd: CancelAdyenTokenRegistrationCommand) {
    apply(
      AdyenTokenRegistrationCanceledEvent(
        cmd.adyenTokenRegistrationId,
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
    this.isForPayout = e.isPayoutSetup
  }

  @EventSourcingHandler
  fun on(e: AdyenTokenRegistrationAuthorisedFromNotificationEvent) {
    TODO("Implement!")
  }

  @EventSourcingHandler
  fun on(e: PendingAdyenTokenRegistrationCreatedEvent) {
    this.adyenTokenRegistrationId = e.adyenTokenRegistrationId
    this.memberId = e.memberId
    this.recurringDetailReference = e.adyenPaymentsResponse.getRecurringDetailReference()
    this.adyenTokenRegistrationStatus = AdyenTokenRegistrationStatus.PENDING
    this.paymentDataFromAction = e.paymentDataFromAction
    this.isForPayout = e.isPayoutSetup
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
