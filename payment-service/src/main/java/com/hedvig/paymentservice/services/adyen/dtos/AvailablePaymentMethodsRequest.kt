package com.hedvig.paymentservice.services.adyen.dtos

import javax.money.MonetaryAmount

data class AvailablePaymentMethodsRequest(
  val desiredAmount: MonetaryAmount,
  val countryCode: String
)
