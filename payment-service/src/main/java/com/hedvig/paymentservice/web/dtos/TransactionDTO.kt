package com.hedvig.paymentservice.web.dtos

import com.hedvig.paymentservice.query.member.entities.Transaction
import java.time.Instant
import java.util.UUID
import javax.money.MonetaryAmount

data class TransactionDTO(
    val id: UUID,
    val amount: MonetaryAmount,
    val timestamp: Instant,
    val transactionType: String?,
    val transactionStatus: String?,
) {
    companion object {
        fun fromTransaction(transaction: Transaction): TransactionDTO {
            return TransactionDTO(
                transaction.id,
                transaction.money,
                transaction.timestamp,
                transaction.transactionType.toString(),
                transaction.transactionStatus.toString()
            )
        }
    }
}
