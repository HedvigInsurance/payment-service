package com.hedvig.paymentservice.domain.adyenTransaction.events

import java.util.UUID
import javax.money.MonetaryAmount

data class AdyenTransactionAutoRescueProcessStartedEvent(
    val transactionId: UUID,
    val memberId: String,
    val amount: MonetaryAmount,
    val reason: String,
    val rescueReference: String
)
