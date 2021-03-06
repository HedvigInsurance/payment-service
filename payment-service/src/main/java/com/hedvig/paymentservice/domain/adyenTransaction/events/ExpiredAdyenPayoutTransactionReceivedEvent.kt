package com.hedvig.paymentservice.domain.adyenTransaction.events

import java.util.UUID
import javax.money.MonetaryAmount

class ExpiredAdyenPayoutTransactionReceivedEvent(
    val transactionId: UUID,
    val memberId: String,
    val amount: MonetaryAmount,
    val reason: String?
)
