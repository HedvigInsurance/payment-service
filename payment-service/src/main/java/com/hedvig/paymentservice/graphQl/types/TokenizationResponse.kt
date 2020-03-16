package com.hedvig.paymentservice.graphQl.types

import com.adyen.model.checkout.PaymentsResponse

data class TokenizationResponse(
  val paymentsResponse: PaymentsResponse
)
