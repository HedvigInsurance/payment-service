package com.hedvig.paymentservice.serviceIntergration.productPricing.dto

import java.time.LocalDate

data class PolicyGuessResponseDto(
  val productType: PolicyType,
  val inceptionInStockholm: LocalDate
)
