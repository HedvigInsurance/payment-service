package com.hedvig.paymentservice.graphQl.types

import com.adyen.model.checkout.PaymentMethodDetails

data class TokenizationRequest(
  val paymentMethodDetails: PaymentMethodDetails,
  val channel: TokenizationChannel,
  val browserInfo: BrowserInfo?,
  val returnUrl: String
)
