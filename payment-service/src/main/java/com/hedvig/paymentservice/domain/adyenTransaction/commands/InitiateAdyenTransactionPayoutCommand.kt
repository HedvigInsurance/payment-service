package com.hedvig.paymentservice.domain.adyenTransaction.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.UUID
import javax.money.MonetaryAmount

data class InitiateAdyenTransactionPayoutCommand(
  @TargetAggregateIdentifier
  val transactionId: UUID,
  val memberId: String,
  val shopperReference: String,
  val amount: MonetaryAmount,
  val email: String
)
