package com.hedvig.paymentservice.serviceIntergration.accountService.dto

import java.util.*

data class NotifyChargeFailedRequestDto(
  val transactionId: UUID
)
