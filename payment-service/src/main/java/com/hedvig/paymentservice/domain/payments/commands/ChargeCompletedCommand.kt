package com.hedvig.paymentservice.domain.payments.commands

import lombok.Value
import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.time.Instant
import java.util.UUID
import javax.money.MonetaryAmount

@Value
data class ChargeCompletedCommand(
  @TargetAggregateIdentifier
  var memberId: String,
  var transactionId: UUID,
  var amount: MonetaryAmount,
  var timestamp: Instant
)
