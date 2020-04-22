package com.hedvig.paymentservice.domain.payments.events

import java.time.Instant
import java.util.UUID

data class PayoutCompletedEvent(
  val memberId: String,
  val transactionId: UUID,
  val timestamp: Instant
)
