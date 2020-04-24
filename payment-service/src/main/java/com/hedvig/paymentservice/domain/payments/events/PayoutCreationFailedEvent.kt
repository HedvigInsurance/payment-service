package com.hedvig.paymentservice.domain.payments.events

import lombok.Value
import java.time.Instant
import java.util.UUID
import javax.money.MonetaryAmount

@Value
data class PayoutCreationFailedEvent(
  val memberId: String,
  val transactionId: UUID,
  val amount: MonetaryAmount,
  val timestamp: Instant
)
