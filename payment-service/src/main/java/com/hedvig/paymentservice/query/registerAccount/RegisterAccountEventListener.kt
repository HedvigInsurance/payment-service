package com.hedvig.paymentservice.query.registerAccount

import com.hedvig.paymentservice.domain.registerAccount.enums.RegisterAccountProcessStatus
import com.hedvig.paymentservice.domain.registerAccount.events.RegisterAccountConfirmationReceivedEvent
import com.hedvig.paymentservice.domain.registerAccount.events.RegisterAccountNotificationReceivedEvent
import com.hedvig.paymentservice.domain.registerAccount.events.RegisterAccountRequestCreatedEvent
import com.hedvig.paymentservice.domain.registerAccount.events.RegisterAccountResponseReceivedEvent
import com.hedvig.paymentservice.query.registerAccount.enteties.RegisterAccount
import com.hedvig.paymentservice.query.registerAccount.enteties.RegisterAccountRepository
import mu.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class RegisterAccountEventListener(
  val repository: RegisterAccountRepository
) {

  @EventListener
  fun on(e: RegisterAccountRequestCreatedEvent) {
    this.repository.save(RegisterAccount(e.hedvigOrderId, e.memberId, RegisterAccountProcessStatus.INITIATED))
  }

  @EventListener
  fun on(e: RegisterAccountResponseReceivedEvent) {
    val optionalRegisterAccount = repository.findById(e.hedvigOrderId)

    if (optionalRegisterAccount.isPresent) {
      val registerAccount = optionalRegisterAccount.get()
      registerAccount.status = RegisterAccountProcessStatus.REQUESTED
      repository.save(registerAccount)
    } else {
      logger.error { "RegisterAccountResponseReceivedEvent - Cannot finn register account for hedvigOrderId: ${e.hedvigOrderId}" }
    }
  }

  @EventListener
  fun on(e: RegisterAccountNotificationReceivedEvent) {
    val optionalRegisterAccount = repository.findById(e.hedvigOrderId)

    if (optionalRegisterAccount.isPresent) {
      val registerAccount = optionalRegisterAccount.get()
      if (registerAccount.status != RegisterAccountProcessStatus.CONFIRMED) {
        registerAccount.status = RegisterAccountProcessStatus.IN_PROGRESS
        repository.save(registerAccount)
      }
    } else {
      logger.error { "RegisterAccountNotificationReceivedEvent - Cannot finn register account for hedvigOrderId: ${e.hedvigOrderId}" }
    }
  }

  @EventListener
  fun on(e: RegisterAccountConfirmationReceivedEvent) {
    val optionalRegisterAccount = repository.findById(e.hedvigOrderId)

    if (optionalRegisterAccount.isPresent) {
      val registerAccount = optionalRegisterAccount.get()
      registerAccount.status = RegisterAccountProcessStatus.CONFIRMED
      repository.save(registerAccount)
    } else {
      logger.error { "RegisterAccountConfirmationReceivedEvent - Cannot finn register account for hedvigOrderId: ${e.hedvigOrderId}" }
    }
  }

}
