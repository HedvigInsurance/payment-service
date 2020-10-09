package com.hedvig.paymentservice.services.adyen.util

import com.hedvig.paymentservice.configuration.MerchantAccounts
import com.hedvig.paymentservice.serviceIntergration.memberService.MemberService
import com.hedvig.paymentservice.serviceIntergration.productPricing.ProductPricingService
import com.hedvig.paymentservice.serviceIntergration.underwriterClient.UnderwriterService
import com.hedvig.paymentservice.services.adyen.dtos.AdyenMerchantInfo
import com.neovisionaries.i18n.CountryCode
import com.neovisionaries.i18n.CurrencyCode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.Market as ProductPricingMarket

@Component
class AdyenMerchantPicker(
  val memberService: MemberService,
  val underwriterService: UnderwriterService,
  val productPricingService: ProductPricingService,
  val merchantAccounts: MerchantAccounts
) {
  fun getAdyenMerchantInfo(memberId: String): AdyenMerchantInfo {
    val marketInfo = getMarketFromContract(memberId)
      ?: getMarketFromQuote(memberId)
      ?: getMarketFromPickedLocale(memberId)
      ?: throw NullPointerException("Could not determine market for member: $memberId")

    return AdyenMerchantInfo(
      account = merchantAccounts.merchantAccounts!![marketInfo.name] ?: error("Cannot fetch merchant account"),
      countryCode = marketInfo.countryCode,
      currencyCode = marketInfo.currencyCode
    )
  }

  private fun getMarketFromContract(memberId: String): Market? {
    return try {
      val contractMarketInfo = productPricingService.getContractMarketInfo(memberId)
      when (contractMarketInfo.market) {
        ProductPricingMarket.SWEDEN -> Market.SWEDEN
        ProductPricingMarket.NORWAY -> Market.NORWAY
        ProductPricingMarket.DENMARK -> Market.DENMARK
      }
    } catch (exception: Exception) {
      logger.error("Cannot find market from contact for member $memberId")
      null
    }
  }

  private fun getMarketFromQuote(memberId: String): Market? {
    return try {
      val quoteMarketInfo = underwriterService.getMarketFromQuote(memberId)
      Market.valueOf(quoteMarketInfo.market)
    } catch (exception: Exception) {
      logger.error("Cannot find market from quotes for member $memberId")
      null
    }
  }

  private fun getMarketFromPickedLocale(receiverMemberId: String): Market? {
    return when (memberService.getPickedLocale(receiverMemberId)) {
      "sv_SE" -> Market.SWEDEN
      "en_SE" -> Market.SWEDEN
      "nb_NO" -> Market.NORWAY
      "en_NO" -> Market.NORWAY
      else -> null
    }
  }

  enum class Market(val currencyCode: CurrencyCode, val countryCode: CountryCode) {
    SWEDEN(currencyCode = CurrencyCode.SEK, countryCode = CountryCode.SE),
    NORWAY(currencyCode = CurrencyCode.NOK, countryCode = CountryCode.NO),
    DENMARK(currencyCode = CurrencyCode.DKK, countryCode = CountryCode.DK)
  }

  companion object {
    val logger = LoggerFactory.getLogger(this::class.java)
  }
}
