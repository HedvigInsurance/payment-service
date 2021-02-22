package com.hedvig.paymentservice.domain.payments.events

import java.time.Instant
import java.util.UUID
import javax.money.MonetaryAmount
import lombok.Value

@Value
data class PayoutCreationFailedEvent(
    val memberId: String,
    val transactionId: UUID,
    val amount: MonetaryAmount,
    val timestamp: Instant
)
