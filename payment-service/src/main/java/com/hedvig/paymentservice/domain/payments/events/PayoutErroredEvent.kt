package com.hedvig.paymentservice.domain.payments.events

import java.time.Instant
import java.util.UUID
import javax.money.MonetaryAmount

data class PayoutErroredEvent(
  val memberId: String,
  val transactionId: UUID,
  val amount: MonetaryAmount,
  val reason: String?,
  val timestamp: Instant
)

