package com.hedvig.paymentservice.domain.registerAccount

import com.hedvig.paymentservice.domain.registerAccount.commands.CreateRegisterAccountRequestCommand
import com.hedvig.paymentservice.domain.registerAccount.commands.ReceiveRegisterAccountConfirmationCommand
import com.hedvig.paymentservice.domain.registerAccount.commands.ReceiveRegisterAccountNotificationCommand
import com.hedvig.paymentservice.domain.registerAccount.commands.ReceiveRegisterAccountResponseCommand
import com.hedvig.paymentservice.domain.registerAccount.enums.RegisterAccountProcessStatus
import com.hedvig.paymentservice.domain.registerAccount.events.RegisterAccountConfirmationReceivedEvent
import com.hedvig.paymentservice.domain.registerAccount.events.RegisterAccountNotificationReceivedEvent
import com.hedvig.paymentservice.domain.registerAccount.events.RegisterAccountRequestCreatedEvent
import com.hedvig.paymentservice.domain.registerAccount.events.RegisterAccountResponseReceivedEvent
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.model.AggregateIdentifier
import org.axonframework.commandhandling.model.AggregateLifecycle.apply
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.spring.stereotype.Aggregate
import java.util.*

@Aggregate
class RegisterAccount() {

  @AggregateIdentifier
  lateinit var hedvigOrderId: UUID
  lateinit var memberId: String
  lateinit var status: RegisterAccountProcessStatus

  @CommandHandler
  constructor(cmd: CreateRegisterAccountRequestCommand) : this() {
    apply(RegisterAccountRequestCreatedEvent(cmd.hedvigOrderId, cmd.memberId))
  }

  @CommandHandler
  fun on(cmd: ReceiveRegisterAccountResponseCommand) {
    apply(RegisterAccountResponseReceivedEvent(cmd.hedvigOrderId))
  }

  @CommandHandler
  fun on(cmd: ReceiveRegisterAccountNotificationCommand) {
    if (this.status != RegisterAccountProcessStatus.CONFIRMED) {
      apply(RegisterAccountNotificationReceivedEvent(cmd.hedvigOrderId, cmd.memberId))
    }
  }

  @CommandHandler
  fun on(cmd: ReceiveRegisterAccountConfirmationCommand) {
    apply(RegisterAccountConfirmationReceivedEvent(cmd.hedvigOrderId, cmd.memberId))
  }

  @EventSourcingHandler
  fun on(e: RegisterAccountRequestCreatedEvent) {
    this.hedvigOrderId = e.hedvigOrderId
    this.memberId = e.memberId
    this.status = RegisterAccountProcessStatus.INITIATED
  }

  @EventSourcingHandler
  fun on(e: RegisterAccountResponseReceivedEvent) {
    this.status = RegisterAccountProcessStatus.REQUESTED
  }

  @EventSourcingHandler
  fun on(e: RegisterAccountNotificationReceivedEvent) {
    this.status = RegisterAccountProcessStatus.IN_PROGRESS

  }

  @EventSourcingHandler
  fun on(e: RegisterAccountConfirmationReceivedEvent) {
    this.status = RegisterAccountProcessStatus.CONFIRMED
  }
}

