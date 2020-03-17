package com.hedvig.paymentservice.graphQl.types

import com.adyen.model.checkout.PaymentMethodsResponse

data class AvailablePaymentMethodsResponse(
  val paymentMethodsResponse: PaymentMethodsResponse
)
