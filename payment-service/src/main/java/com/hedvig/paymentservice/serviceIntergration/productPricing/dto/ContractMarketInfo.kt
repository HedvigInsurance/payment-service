package com.hedvig.paymentservice.serviceIntergration.productPricing.dto

import javax.money.CurrencyUnit

data class ContractMarketInfo(
  val market: Market,
  val preferredCurrency: CurrencyUnit
)


