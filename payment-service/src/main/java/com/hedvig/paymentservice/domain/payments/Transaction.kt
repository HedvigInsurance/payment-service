package com.hedvig.paymentservice.domain.payments

import java.time.Instant
import java.util.UUID
import javax.money.MonetaryAmount

data class Transaction(
    val transactionId: UUID,
    val amount: MonetaryAmount,
    val timestamp: Instant
) {
  lateinit var transactionType: TransactionType
  lateinit var transactionStatus: TransactionStatus
}
