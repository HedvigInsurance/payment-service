package com.hedvig.paymentservice.domain.adyenTransaction.events

import java.util.UUID
import javax.money.MonetaryAmount

data class AdyenTransactionAutoRescueProcessStartedReceivedEvent(
    val transactionId: UUID,
    val memberId: String,
    val amount: MonetaryAmount,
    val refusalReason: String,
    val rescueReference: String
)
