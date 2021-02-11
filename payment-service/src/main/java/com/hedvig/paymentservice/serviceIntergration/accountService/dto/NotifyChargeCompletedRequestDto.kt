package com.hedvig.paymentservice.serviceIntergration.accountService.dto

import java.time.Instant
import java.util.*
import javax.money.MonetaryAmount

data class NotifyChargeCompletedRequestDto(
    val transactionId: UUID,
    val amount: MonetaryAmount,
    val chargedAt: Instant
)
