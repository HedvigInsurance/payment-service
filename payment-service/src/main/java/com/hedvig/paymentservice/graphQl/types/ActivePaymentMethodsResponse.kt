package com.hedvig.paymentservice.graphQl.types

import com.adyen.model.checkout.PaymentMethodsResponse

data class ActivePaymentMethodsResponse(
  val paymentMethodsResponse: PaymentMethodsResponse
)
