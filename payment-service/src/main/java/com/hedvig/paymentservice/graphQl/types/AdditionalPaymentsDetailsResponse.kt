package com.hedvig.paymentservice.graphQl.types

import com.adyen.model.checkout.PaymentsResponse

data class AdditionalPaymentsDetailsResponse(
  val paymentsResponse: PaymentsResponse
)
