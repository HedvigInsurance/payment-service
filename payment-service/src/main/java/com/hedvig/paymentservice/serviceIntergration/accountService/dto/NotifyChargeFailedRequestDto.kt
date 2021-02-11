package com.hedvig.paymentservice.serviceIntergration.accountService.dto

import java.time.Instant
import java.util.*

data class NotifyChargeFailedRequestDto(
    val transactionId: UUID,
    val failedAt: Instant
)
