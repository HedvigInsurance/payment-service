package com.hedvig.paymentservice.graphQl.types

import com.hedvig.paymentservice.services.adyen.dtos.StoredPaymentMethodsDetails

data class ActivePaymentMethodsResponse(
  val storedPaymentMethodsDetails: StoredPaymentMethodsDetails
)
