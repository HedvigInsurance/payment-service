package com.hedvig.paymentservice.domain.payments.events

import com.adyen.model.checkout.PaymentsResponse

class AdyenAccountUpdatedEvent(
  val memberId: String,
  val adyenTokenId: String,
  val recurringDetailReference: String,
  val tokenStatus: PaymentsResponse.ResultCodeEnum
)
