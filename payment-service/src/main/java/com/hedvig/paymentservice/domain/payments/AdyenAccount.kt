package com.hedvig.paymentservice.domain.payments

import com.hedvig.paymentservice.domain.payments.enums.AdyenAccountStatus

data class AdyenAccount(
  val recurringDetailReference: String,
  val status: AdyenAccountStatus
)
