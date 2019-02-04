package com.hedvig.paymentservice.query.registerAccount

import com.hedvig.paymentservice.domain.accountRegistration.enums.AccountRegistrationStatus
import com.hedvig.paymentservice.domain.accountRegistration.events.AccountRegistrationConfirmationReceivedEvent
import com.hedvig.paymentservice.domain.accountRegistration.events.AccountRegistrationNotificationReceivedEvent
import com.hedvig.paymentservice.domain.accountRegistration.events.AccountRegistrationRequestCreatedEvent
import com.hedvig.paymentservice.domain.accountRegistration.events.AccountRegistrationResponseReceivedEvent
import com.hedvig.paymentservice.query.registerAccount.enteties.AccountRegistration
import com.hedvig.paymentservice.query.registerAccount.enteties.AccountRegistrationRepository
import mu.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class RegisterAccountEventListener(
  val repository: AccountRegistrationRepository
) {

  @EventListener
  fun on(e: AccountRegistrationRequestCreatedEvent) {
    this.repository.save(AccountRegistration(e.accountRegistrationId, e.memberId, AccountRegistrationStatus.INITIATED))
  }

  @EventListener
  fun on(e: AccountRegistrationResponseReceivedEvent) {
    val optionalRegisterAccount = repository.findById(e.accountRegistrationId)

    if (optionalRegisterAccount.isPresent) {
      val registerAccount = optionalRegisterAccount.get()
      registerAccount.status = AccountRegistrationStatus.REQUESTED
      repository.save(registerAccount)
    } else {
      logger.error { "RegisterAccountResponseReceivedEvent - Cannot finn register account for accountRegistrationId: ${e.accountRegistrationId}" }
    }
  }

  @EventListener
  fun on(e: AccountRegistrationNotificationReceivedEvent) {
    val optionalRegisterAccount = repository.findById(e.accountRegistrationId)

    if (optionalRegisterAccount.isPresent) {
      val registerAccount = optionalRegisterAccount.get()
      registerAccount.status = AccountRegistrationStatus.IN_PROGRESS
      repository.save(registerAccount)
    } else {
      logger.error { "RegisterAccountNotificationReceivedEvent - Cannot finn register account for accountRegistrationId: ${e.accountRegistrationId}" }
    }
  }

  @EventListener
  fun on(e: AccountRegistrationConfirmationReceivedEvent) {
    val optionalRegisterAccount = repository.findById(e.accountRegistrationId)

    if (optionalRegisterAccount.isPresent) {
      val registerAccount = optionalRegisterAccount.get()
      registerAccount.status = AccountRegistrationStatus.CONFIRMED
      repository.save(registerAccount)
    } else {
      logger.error { "RegisterAccountConfirmationReceivedEvent - Cannot finn register account for accountRegistrationId: ${e.accountRegistrationId}" }
    }
  }

}
