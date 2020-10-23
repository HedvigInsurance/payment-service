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
  lateinit var adyenMerchantAccount: String
  var recurringDetailReference: String? = null
  var paymentDataFromAction: String? = null
  var isForPayout: Boolean = false
  var shopperReference: String? = null

  @CommandHandler
  constructor(cmd: CreateAuthorisedAdyenTokenRegistrationCommand) : this() {
    apply(
      AdyenTokenRegistrationAuthorisedEvent(
        adyenTokenRegistrationId = cmd.adyenTokenRegistrationId,
        memberId = cmd.memberId,
        adyenPaymentsResponse = cmd.adyenPaymentsResponse,
        adyenMerchantAccount = cmd.adyenMerchantInfo.account,
        isPayoutSetup = cmd.isPayoutSetup,
        shopperReference = cmd.shopperReference
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
        cmd.adyenMerchantInfo.account,
        cmd.isPayoutSetup,
        cmd.shopperReference
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
        adyenMerchantAccount,
        false,
        cmd.shopperReference
      )
    )
  }

  @CommandHandler
  fun handle(cmd: AuthoriseAdyenTokenRegistrationFromNotificationCommand) {
    apply(
      AdyenTokenRegistrationAuthorisedFromNotificationEvent(
        cmd.adyenTokenRegistrationId,
        cmd.memberId,
        cmd.adyenNotification,
        cmd.shopperReference
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
    this.adyenMerchantAccount = e.adyenMerchantAccount
    this.isForPayout = e.isPayoutSetup
    this.shopperReference = e.shopperReference
  }

  @EventSourcingHandler
  fun on(e: AdyenTokenRegistrationAuthorisedFromNotificationEvent) {
    this.adyenTokenRegistrationId = e.adyenTokenRegistrationId
    this.memberId = e.memberId
    //TODO: To be future proof maybe we should look at the notification item to see if we can add recurring payment details
    this.adyenTokenRegistrationStatus = AdyenTokenRegistrationStatus.AUTHORISED
  }

  @EventSourcingHandler
  fun on(e: PendingAdyenTokenRegistrationCreatedEvent) {
    this.adyenTokenRegistrationId = e.adyenTokenRegistrationId
    this.memberId = e.memberId
    this.recurringDetailReference = e.adyenPaymentsResponse.getRecurringDetailReference()
    this.adyenTokenRegistrationStatus = AdyenTokenRegistrationStatus.PENDING
    this.paymentDataFromAction = e.paymentDataFromAction
    this.adyenMerchantAccount = e.adyenMerchantAccount
    this.isForPayout = e.isPayoutSetup
    this.shopperReference = e.shopperReference
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
