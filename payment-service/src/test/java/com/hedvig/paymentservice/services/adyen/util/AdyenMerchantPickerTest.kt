package com.hedvig.paymentservice.services.adyen.util

import com.hedvig.paymentservice.configuration.MerchantAccounts
import com.hedvig.paymentservice.query.adyenAccount.MemberAdyenAccount
import com.hedvig.paymentservice.query.adyenAccount.MemberAdyenAccountRepository
import com.hedvig.paymentservice.serviceIntergration.memberService.MemberService
import com.hedvig.paymentservice.serviceIntergration.productPricing.ProductPricingService
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.ContractMarketInfo
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.Market
import com.hedvig.paymentservice.serviceIntergration.underwriterClient.UnderwriterService
import com.hedvig.paymentservice.serviceIntergration.underwriterClient.dtos.QuoteMarketInfo
import com.hedvig.paymentservice.services.adyen.dtos.AdyenMerchantInfo
import com.neovisionaries.i18n.CountryCode
import com.neovisionaries.i18n.CurrencyCode
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringRunner
import java.util.Optional
import javax.money.Monetary

@RunWith(SpringRunner::class)
class AdyenMerchantPickerTest() {
    @MockkBean
    lateinit var memberService: MemberService

    @MockkBean
    lateinit var underwriterService: UnderwriterService

    @MockkBean
    lateinit var productPricingService: ProductPricingService

    @MockkBean
    lateinit var merchantAccounts: MerchantAccounts

    @MockkBean
    lateinit var memberAdyenAccountRepository: MemberAdyenAccountRepository

    lateinit var adyenMerchantPicker: AdyenMerchantPicker

    @Before
    fun setup() {
        adyenMerchantPicker = AdyenMerchantPicker(
            memberService = memberService,
            underwriterService = underwriterService,
            productPricingService = productPricingService,
            memberAdyenAccountRepository = memberAdyenAccountRepository,
            merchantAccounts = makeMerchantAccount()
        )

        every { memberAdyenAccountRepository.findById(any()) } returns Optional.empty()
    }

    @Test
    fun `Given payment-service, contracts and quotes failed to extract the market, get the market info from picked Locale`() {
        every { memberService.getPickedLocale(any()) } returns "en_DK"
        val x = adyenMerchantPicker.getAdyenMerchantInfo("1234")

        assertThat(x).isEqualTo(
            AdyenMerchantInfo(
                account = "HedvigTestDenmark",
                countryCode = CountryCode.DK,
                currencyCode = CurrencyCode.DKK
            )
        )
    }

    @Test
    fun `Given only payment-service can extract the market, get the market info from member entity`() {
        val account = MemberAdyenAccount("1234", "HedvigTestDenmark")

        every { memberAdyenAccountRepository.findById(any()) } returns Optional.of(account)

        val x = adyenMerchantPicker.getAdyenMerchantInfo("1234")

        assertThat(x).isEqualTo(
            AdyenMerchantInfo(
                account = "HedvigTestDenmark",
                countryCode = CountryCode.DK,
                currencyCode = CurrencyCode.DKK
            )
        )
    }

    @Test
    fun `Given only contact&aggrement can extract the market, get the market info from ContractMarketInfo`() {
        every { productPricingService.getContractMarketInfo(any()) } returns ContractMarketInfo(
            Market.DENMARK,
            preferredCurrency = Monetary.getCurrency("DKK")
        )

        val x = adyenMerchantPicker.getAdyenMerchantInfo("1234")

        assertThat(x).isEqualTo(
            AdyenMerchantInfo(
                account = "HedvigTestDenmark",
                countryCode = CountryCode.DK,
                currencyCode = CurrencyCode.DKK
            )
        )
    }

    @Test
    fun `Given only quotes can extract the market, get the market info from QuoteMarketInfo`() {
        every { underwriterService.getMarketFromQuote(any()) } returns QuoteMarketInfo(
            "DENMARK"
        )

        val x = adyenMerchantPicker.getAdyenMerchantInfo("1234")

        assertThat(x).isEqualTo(
            AdyenMerchantInfo(
                account = "HedvigTestDenmark",
                countryCode = CountryCode.DK,
                currencyCode = CurrencyCode.DKK
            )
        )
    }

    private fun makeMerchantAccount(): MerchantAccounts {
        val merchantAccounts = MerchantAccounts()
        merchantAccounts.merchantAccounts = mapOf<String, String>(
            Pair("NORWAY", "HedvigTestNorway"),
            Pair("DENMARK", "HedvigTestDenmark"),
            Pair("SWEDEN", "HedvigTestSweden")
        )

        return merchantAccounts
    }
}

