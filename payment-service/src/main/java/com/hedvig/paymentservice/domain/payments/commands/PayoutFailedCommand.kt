package com.hedvig.paymentservice.domain.payments.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.time.Instant
import java.util.UUID
import javax.money.MonetaryAmount

data class PayoutFailedCommand(
  @TargetAggregateIdentifier
  val memberId: String,
  val transactionId: UUID,
  val amount: MonetaryAmount,
  val timestamp: Instant
)
