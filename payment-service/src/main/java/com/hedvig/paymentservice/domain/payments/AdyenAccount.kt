package com.hedvig.paymentservice.domain.payments

import com.adyen.model.checkout.PaymentsResponse

data class AdyenAccount(
  val adyenTokenId: String,
  val recurringDetailReference: String,
  val status: PaymentsResponse.ResultCodeEnum
)
