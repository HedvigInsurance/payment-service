package com.hedvig.paymentservice.serviceIntergration.accountService.dto

import java.util.*
import javax.money.MonetaryAmount

data class NotifyChargeFailedRequestDto(
  val transactionId: UUID,
  val amount: MonetaryAmount
)
