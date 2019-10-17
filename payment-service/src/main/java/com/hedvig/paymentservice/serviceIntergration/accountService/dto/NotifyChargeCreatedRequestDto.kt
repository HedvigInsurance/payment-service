package com.hedvig.paymentservice.serviceIntergration.accountService.dto

import java.time.Instant
import java.util.*
import javax.money.MonetaryAmount

data class NotifyChargeCreatedRequestDto(
  val transactionId: UUID,
  val amount: MonetaryAmount,
  val initiatedBy: String?,
  val createdAt: Instant
)
