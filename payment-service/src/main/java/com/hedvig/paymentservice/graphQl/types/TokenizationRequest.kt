package com.hedvig.paymentservice.graphQl.types

import com.adyen.model.checkout.PaymentsRequest

data class TokenizationRequest(
  val paymentsRequest: PaymentsRequest
)
