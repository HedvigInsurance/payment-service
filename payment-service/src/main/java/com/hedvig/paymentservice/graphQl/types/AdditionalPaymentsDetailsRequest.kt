package com.hedvig.paymentservice.graphQl.types

import com.adyen.model.checkout.PaymentsDetailsRequest

data class AdditionalPaymentsDetailsRequest(
  val paymentsDetailsRequest: PaymentsDetailsRequest
)
