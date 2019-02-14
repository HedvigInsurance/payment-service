package com.hedvig.paymentservice.query.registerAccount

import com.hedvig.paymentservice.domain.accountRegistration.enums.AccountRegistrationStatus
import com.hedvig.paymentservice.domain.accountRegistration.events.*
import com.hedvig.paymentservice.query.registerAccount.enteties.AccountRegistration
import com.hedvig.paymentservice.query.registerAccount.enteties.AccountRegistrationRepository
import mu.KotlinLogging
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.Timestamp
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

private val logger = KotlinLogging.logger {}

@Component
@Transactional
class AccountRegistrationEventListener(
  val repository: AccountRegistrationRepository
) {

  @EventHandler
  fun on(e: AccountRegistrationRequestCreatedEvent, @Timestamp timestamp: Instant) {

    logger.info { "AccountRegistrationRequestCreatedEvent Saved" }

    this.repository.save(
      AccountRegistration(
        e.accountRegistrationId,
        e.memberId,
        AccountRegistrationStatus.INITIATED,
        e.hedvigOrderId,
        timestamp
      )
    )
  }

  @EventHandler
  fun on(e: AccountRegistrationResponseReceivedEvent) {
    val optionalRegisterAccount = repository.findById(e.accountRegistrationId)

    if (optionalRegisterAccount.isPresent) {
      val registerAccount = optionalRegisterAccount.get()
      registerAccount.status = AccountRegistrationStatus.REQUESTED
      repository.save(registerAccount)
    } else {
      logger.error { "AccountRegistrationResponseReceivedEvent - Cannot find register account for accountRegistrationId: ${e.accountRegistrationId}" }
    }
  }

  @EventHandler
  fun on(e: AccountRegistrationNotificationReceivedEvent) {
    val optionalRegisterAccount = repository.findById(e.accountRegistrationId)

    if (optionalRegisterAccount.isPresent) {
      val registerAccount = optionalRegisterAccount.get()
      registerAccount.status = AccountRegistrationStatus.IN_PROGRESS
      repository.save(registerAccount)
    } else {
      logger.error { "AccountRegistrationNotificationReceivedEvent - Cannot find register account for accountRegistrationId: ${e.accountRegistrationId}" }
    }
  }

  @EventHandler
  fun on(e: AccountRegistrationConfirmationReceivedEvent) {
    val optionalRegisterAccount = repository.findById(e.accountRegistrationId)

    if (optionalRegisterAccount.isPresent) {
      val registerAccount = optionalRegisterAccount.get()
      registerAccount.status = AccountRegistrationStatus.CONFIRMED
      repository.save(registerAccount)
    } else {
      logger.error { "AccountRegistrationConfirmationReceivedEvent - Cannot find register account for accountRegistrationId: ${e.accountRegistrationId}" }
    }
  }

  @EventHandler
  fun on(e: AccountRegistrationCancellationReceivedEvent) {
    val optionalRegisterAccount = repository.findById(e.accountRegistrationId)

    if (optionalRegisterAccount.isPresent) {
      val registerAccount = optionalRegisterAccount.get()
      registerAccount.status = AccountRegistrationStatus.CANCELLED
      repository.save(registerAccount)
    } else {
      logger.error { "AccountRegistrationCancellationReceivedEvent - Cannot find register account for accountRegistrationId: ${e.accountRegistrationId}" }
    }
  }
}
