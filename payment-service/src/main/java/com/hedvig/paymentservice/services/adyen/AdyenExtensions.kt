package com.hedvig.paymentservice.services.adyen

import javax.money.MonetaryAmount

/**
 * based on https://docs.adyen.com/development-resources/currency-codes
 */
fun MonetaryAmount.toAdyenMinorUnits(): Long = when (this.currency.currencyCode) {
  "SEK",
  "NOK" -> this.number.doubleValueExact().times(100).toLong()
  else -> throw RuntimeException("The currency ${this.currency.currencyCode} is not mapped to Adyen minor units")
}
