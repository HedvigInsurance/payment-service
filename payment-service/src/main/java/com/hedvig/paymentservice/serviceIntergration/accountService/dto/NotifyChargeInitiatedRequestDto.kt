package com.hedvig.paymentservice.serviceIntergration.accountService.dto

import java.time.Instant
import java.util.*
import javax.money.MonetaryAmount

data class NotifyChargeInitiatedRequestDto(
  val transactionId: UUID,
  val amount: MonetaryAmount,
  val initiatedBy: String?,
  val initiatedAt: Instant
)
