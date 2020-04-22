package com.hedvig.paymentservice.domain.payments.events

import java.time.Instant
import java.util.UUID
import javax.money.MonetaryAmount

data class ChargeCreationFailedEvent(
  val memberId: String,
  val transactionId: UUID,
  val amount: MonetaryAmount,
  val timestamp: Instant,
  val reason: String
)
