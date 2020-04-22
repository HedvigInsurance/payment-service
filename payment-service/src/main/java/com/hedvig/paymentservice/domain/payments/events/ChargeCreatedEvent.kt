package com.hedvig.paymentservice.domain.payments.events

import org.axonframework.serialization.Revision
import java.time.Instant
import java.util.UUID
import javax.money.MonetaryAmount

@Revision("1.0")
data class ChargeCreatedEvent(
  val memberId: String,
  val transactionId: UUID,
  val amount: MonetaryAmount,

  val timestamp: Instant,
  val accountId //providerId
  : String,
  //provider ENUM
  val email: String,
  var createdBy: String
)
