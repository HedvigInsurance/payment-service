package com.hedvig.paymentservice.domain.adyenTransaction.events

import java.util.UUID
import javax.money.MonetaryAmount

data class AdyenPayoutTransactionFailedEvent(
    val transactionId: UUID,
    val memberId: String,
    val shopperReference: String,
    val amount: MonetaryAmount,
    val message: String?
)
