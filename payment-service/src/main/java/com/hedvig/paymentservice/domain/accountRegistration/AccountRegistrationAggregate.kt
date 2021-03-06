package com.hedvig.paymentservice.domain.accountRegistration

import com.hedvig.paymentservice.domain.accountRegistration.commands.*
import com.hedvig.paymentservice.domain.accountRegistration.enums.AccountRegistrationStatus
import com.hedvig.paymentservice.domain.accountRegistration.events.*
import java.util.*
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.model.AggregateIdentifier
import org.axonframework.commandhandling.model.AggregateLifecycle.apply
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.spring.stereotype.Aggregate

@Aggregate
class AccountRegistrationAggregate() {

  @AggregateIdentifier
  lateinit var accountRegistrationId: UUID
  lateinit var hedvigOrderId: String
  lateinit var trustlyOrderId: String
  lateinit var memberId: String
  lateinit var status: AccountRegistrationStatus

  @CommandHandler
  constructor(cmd: CreateAccountRegistrationRequestCommand) : this() {
    apply(
      AccountRegistrationRequestCreatedEvent(
        cmd.accountRegistrationId,
        cmd.hedvigOrderId,
        cmd.memberId,
        cmd.trustlyOrderId,
        cmd.trustlyUrl
      )
    )
  }

  @CommandHandler
  fun on(cmd: ReceiveAccountRegistrationResponseCommand) {
    apply(AccountRegistrationResponseReceivedEvent(cmd.accountRegistrationId))
  }

  @CommandHandler
  fun on(cmd: ReceiveAccountRegistrationNotificationCommand) {
    if (this.status != AccountRegistrationStatus.CONFIRMED) {
      apply(AccountRegistrationNotificationReceivedEvent(cmd.accountRegistrationId, cmd.memberId))
    }
  }

  @CommandHandler
  fun on(cmd: ReceiveAccountRegistrationConfirmationCommand) {
    apply(AccountRegistrationConfirmationReceivedEvent(cmd.accountRegistrationId, cmd.memberId))
  }

  @CommandHandler
  fun on(cmd: ReceiveAccountRegistrationCancellationCommand) {
    apply(AccountRegistrationCancellationReceivedEvent(cmd.accountRegistrationId, cmd.hedvigOrderId, cmd.memberId))
  }

  @EventSourcingHandler
  fun on(e: AccountRegistrationRequestCreatedEvent) {
    this.accountRegistrationId = e.accountRegistrationId
    this.hedvigOrderId = e.trustlyOrderId
    this.trustlyOrderId = e.trustlyOrderId
    this.memberId = e.memberId
    this.status = AccountRegistrationStatus.INITIATED
  }

  @EventSourcingHandler
  fun on(e: AccountRegistrationResponseReceivedEvent) {
    this.status = AccountRegistrationStatus.REQUESTED
  }

  @EventSourcingHandler
  fun on(e: AccountRegistrationNotificationReceivedEvent) {
    this.status = AccountRegistrationStatus.IN_PROGRESS
  }

  @EventSourcingHandler
  fun on(e: AccountRegistrationConfirmationReceivedEvent) {
    this.status = AccountRegistrationStatus.CONFIRMED
  }

  @EventSourcingHandler
  fun on(e: AccountRegistrationCancellationReceivedEvent) {
    this.status = AccountRegistrationStatus.CANCELLED
  }
}
