package com.hedvig.paymentservice.web.dtos

import java.time.Instant
import java.util.UUID
import javax.money.MonetaryAmount

class CompleteChargeRequestDTO(
    val memberId: String,
    val transactionId: UUID,
    val amount: MonetaryAmount,
    val timestamp: Instant?
)
