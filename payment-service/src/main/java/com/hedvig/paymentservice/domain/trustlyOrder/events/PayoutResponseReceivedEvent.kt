package com.hedvig.paymentservice.domain.trustlyOrder.events

import java.util.*
import javax.money.MonetaryAmount

data class PayoutResponseReceivedEvent(
    val hedvigOrderId: UUID,
    val memberId: String,
    val amount: MonetaryAmount,
    val transactionId: UUID
)
