package com.hedvig.paymentservice.domain.payments.events

import com.adyen.model.checkout.PaymentsResponse

data class AdyenAccountCreatedEvent(
  val memberId: String,
  val adyenTokenId: String,
  val recurringDetailReference: String,
  val tokenStatus: PaymentsResponse.ResultCodeEnum
)
