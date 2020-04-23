package com.hedvig.paymentservice.domain.adyenTransaction.events

import java.util.UUID
import javax.money.MonetaryAmount

class AdyenTransactionCanceledEvent(
  val transactionId: UUID,
  val memberId: String,
  val recurringDetailReference: String,
  val amount: MonetaryAmount,
  val reason: String
)

