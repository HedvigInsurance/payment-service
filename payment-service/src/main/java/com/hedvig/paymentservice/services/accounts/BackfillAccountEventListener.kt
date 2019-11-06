package com.hedvig.paymentservice.services.accounts

import com.hedvig.paymentservice.domain.payments.events.ChargeCompletedEvent
import com.hedvig.paymentservice.serviceIntergration.accountService.AccountService
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.Timestamp
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.Instant

@Component
@Profile("BackfillCharges")
@ProcessingGroup("BackfillCharges")
class BackfillAccountEventListener @Autowired constructor(
  private val accountService: AccountService
) {

  private val logger = LoggerFactory.getLogger(BackfillAccountEventListener::class.java)

  private val timeOfFirstAutomaticPaymentCharge = Instant.parse("2019-05-31T15:10:00.00Z")

  @EventHandler
  fun on(event: ChargeCompletedEvent, @Timestamp timestamp: Instant) {
      if (timestamp.isBefore(timeOfFirstAutomaticPaymentCharge)) {
        accountService.notifyBackfilledChargeCompleted(event.memberId, event.transactionId, event.amount, timestamp)
        logger.info("Backfill notification of COMPLETED charge on memberId={} of amount={} on time {}", event.memberId, event.amount, timestamp)
      } else {
        logger.info("No charge notification sent to account-service since the event is too new")
      }
    }
}
