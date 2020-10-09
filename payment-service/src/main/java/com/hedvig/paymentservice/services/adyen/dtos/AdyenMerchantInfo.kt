package com.hedvig.paymentservice.services.adyen.dtos

import com.neovisionaries.i18n.CountryCode
import com.neovisionaries.i18n.CurrencyCode

data class AdyenMerchantInfo(
  val account: String,
  val countryCode: CountryCode,
  val currencyCode: CurrencyCode
)
