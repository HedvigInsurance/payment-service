package com.hedvig.paymentservice.domain.adyenTransaction.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.UUID
import javax.money.MonetaryAmount

class ReceivePendingResponseAdyenTransaction(
  @TargetAggregateIdentifier
  val transactionId: UUID,
  val memberId: String,
  val recurringDetailReference: String,
  val amount: MonetaryAmount,
  val reason: String
)
