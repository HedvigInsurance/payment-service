package com.hedvig.paymentservice.domain.adyenTransaction.events

import java.util.UUID
import javax.money.MonetaryAmount

class AdyenPayoutTransactionConfirmedEvent(
  val transactionId: UUID,
  val memberId: String,
  val shopperReference: String,
  val amount: MonetaryAmount,
  val payoutReference: String,
  val response: String
)
