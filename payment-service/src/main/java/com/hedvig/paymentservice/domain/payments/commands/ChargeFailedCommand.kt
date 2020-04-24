package com.hedvig.paymentservice.domain.payments.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.UUID

data class ChargeFailedCommand(
  @TargetAggregateIdentifier
  val memberId: String,
  val transactionId: UUID
)
