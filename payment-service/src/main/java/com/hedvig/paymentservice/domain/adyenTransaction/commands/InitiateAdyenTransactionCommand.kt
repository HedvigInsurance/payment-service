package com.hedvig.paymentservice.domain.adyenTransaction.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.UUID
import javax.money.MonetaryAmount

data class InitiateAdyenTransactionCommand(
  @TargetAggregateIdentifier
  val transactionId: UUID,
  val memberId: String,
  val recurringDetailReference: String,
  val amount: MonetaryAmount
)
