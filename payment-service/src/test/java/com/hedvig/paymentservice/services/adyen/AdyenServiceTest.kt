package com.hedvig.paymentservice.services.adyen

import com.adyen.model.checkout.PaymentMethod
import com.adyen.model.checkout.PaymentMethodsResponse
import com.adyen.model.checkout.StoredPaymentMethod
import com.adyen.service.Checkout
import com.adyen.service.Payout
import com.hedvig.paymentservice.common.UUIDGenerator
import com.hedvig.paymentservice.query.adyenTokenRegistration.entities.AdyenTokenRegistrationRepository
import com.hedvig.paymentservice.query.adyenTransaction.entities.AdyenPayoutTransactionRepository
import com.hedvig.paymentservice.query.adyenTransaction.entities.AdyenTransactionRepository
import com.hedvig.paymentservice.query.member.entities.MemberRepository
import com.hedvig.paymentservice.serviceIntergration.memberService.MemberService
import com.hedvig.paymentservice.services.adyen.dtos.AdyenMerchantInfo
import com.hedvig.paymentservice.services.adyen.util.AdyenMerchantPicker
import com.neovisionaries.i18n.CountryCode
import com.neovisionaries.i18n.CurrencyCode
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import jdk.nashorn.internal.ir.annotations.Ignore
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.commandhandling.gateway.CommandGateway
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
class AdyenServiceTest {

    @MockkBean
    lateinit var adyenCheckout: Checkout

    @MockkBean
    lateinit var adyenPayout: Payout

    @MockkBean
    lateinit var memberRepository: MemberRepository

    @MockkBean
    lateinit var uuidGenerator: UUIDGenerator

    @MockkBean
    lateinit var memberService: MemberService

    @MockkBean
    lateinit var commandGateway: CommandGateway

    @MockkBean
    lateinit var adyenTokenRegistrationRepository: AdyenTokenRegistrationRepository

    @MockkBean
    lateinit var adyenTransactionRepository: AdyenTransactionRepository

    @MockkBean
    lateinit var adyenPayoutTransactionRepository: AdyenPayoutTransactionRepository

    @MockkBean
    lateinit var adyenMerchantPicker: AdyenMerchantPicker


    lateinit var adyenService: AdyenService

    @Before
    fun setup() {
        adyenService = AdyenServiceImpl(
            adyenCheckout,
            adyenPayout,
            adyenPayout,
            memberRepository,
            uuidGenerator,
            memberService,
            commandGateway,
            adyenTokenRegistrationRepository,
            adyenTransactionRepository,
            adyenPayoutTransactionRepository,
            adyenMerchantPicker = adyenMerchantPicker,
            allow3DS2 = true,
            adyenPublicKey = "",
            autoRescueScenario = null
        )
    }

    @Test
    fun `expect that trustly will be excluded from payment methods if the merchant account includes them`() {
        every { adyenMerchantPicker.getAdyenMerchantInfo(any()) } returns AdyenMerchantInfo(
            "account",
            CountryCode.NO,
            CurrencyCode.NOK
        )

        every { adyenCheckout.paymentMethods(any()) } returns
            makePaymentMethodResponse()

        val test = adyenService.getAvailablePayinMethods("1234")

        assertThat(test.paymentMethodsResponse)
            .matches { paymentMethodsResponse ->
                paymentMethodsResponse.paymentMethods
                    .none { paymentMethod -> paymentMethod.type == "trustly" }
            }
        assertThat(test.paymentMethodsResponse)
            .matches { paymentMethodsResponse ->
                paymentMethodsResponse.paymentMethods
                    .any { paymentMethod -> paymentMethod.type == "scheme" }
            }
    }

    @Test
    fun `expect that trustly will be excluded from payment methods if the merchant account does not include them`() {
        every { adyenMerchantPicker.getAdyenMerchantInfo(any()) } returns AdyenMerchantInfo(
            "account",
            CountryCode.NO,
            CurrencyCode.NOK
        )

        every { adyenCheckout.paymentMethods(any()) } returns
            makePaymentMethodResponse(isTrustlyIncluded = false)

        val test = adyenService.getAvailablePayinMethods("1234")

        assertThat(test.paymentMethodsResponse)
            .matches { paymentMethodsResponse ->
                paymentMethodsResponse.paymentMethods
                    .none { paymentMethod -> paymentMethod.type == "trustly" }
            }
        assertThat(test.paymentMethodsResponse)
            .matches { paymentMethodsResponse ->
                paymentMethodsResponse.paymentMethods
                    .any { paymentMethod -> paymentMethod.type == "scheme" }
            }
    }

    @Test
    fun `expect only trustly will be included in the payout methods if the merchant account includes them`() {
        every { adyenMerchantPicker.getAdyenMerchantInfo(any()) } returns AdyenMerchantInfo(
            "account",
            CountryCode.NO,
            CurrencyCode.NOK
        )

        every { adyenCheckout.paymentMethods(any()) } returns
            makePaymentMethodResponse(isTrustlyIncluded = true)

        val test = adyenService.getAvailablePayoutMethods("1234")

        assertThat(test.paymentMethodsResponse)
            .matches { paymentMethodsResponse ->
                paymentMethodsResponse.paymentMethods
                    .any { paymentMethod -> paymentMethod.type == "trustly" }
            }
        assertThat(test.paymentMethodsResponse.paymentMethods.size).isEqualTo(1)
        assertThat(test.paymentMethodsResponse)
            .matches { paymentMethodsResponse ->
                paymentMethodsResponse.paymentMethods
                    .none { paymentMethod -> paymentMethod.type == "scheme" }
            }
    }

    @Test
    fun `expect empty list of payout methods if the merchant account doesnt include trustly`() {
        every { adyenMerchantPicker.getAdyenMerchantInfo(any()) } returns AdyenMerchantInfo(
            "account",
            CountryCode.NO,
            CurrencyCode.NOK
        )

        every { adyenCheckout.paymentMethods(any()) } returns
            makePaymentMethodResponse(isTrustlyIncluded = false)

        val test = adyenService.getAvailablePayoutMethods("1234")

        assertThat(test.paymentMethodsResponse.paymentMethods.size).isEqualTo(0)
    }

    @Test
    @Ignore
    fun `expect null active payout methods if the merchant account doesnt include trustly`() {
        every { adyenMerchantPicker.getAdyenMerchantInfo(any()) } returns AdyenMerchantInfo(
            "account",
            CountryCode.NO,
            CurrencyCode.NOK
        )

        every { adyenCheckout.paymentMethods(any()) } returns
            makeStoredPaymentMethods(isTrustlyIncluded = false)

        val test = adyenService.getActivePayoutMethods("1234")

        assertThat(test?.storedPaymentMethodsDetails).isNull()
    }

    @Test
    @Ignore
    fun `expect valid active payout methods if the merchant account includes trustly`() {
        every { adyenMerchantPicker.getAdyenMerchantInfo(any()) } returns AdyenMerchantInfo(
            "account",
            CountryCode.NO,
            CurrencyCode.NOK
        )

        every { adyenCheckout.paymentMethods(any()) } returns
            makeStoredPaymentMethods()

        val test = adyenService.getActivePayoutMethods("1234")

        assertThat(test?.storedPaymentMethodsDetails).isNotNull
        assertThat(test?.storedPaymentMethodsDetails?.brand).isEqualTo( "trustly")
    }

    private fun makePaymentMethodResponse(isTrustlyIncluded: Boolean = true): PaymentMethodsResponse {
        val response = PaymentMethodsResponse()

        val cardMethod = PaymentMethod().apply { type = "scheme" }
        val trustlyyMethod = PaymentMethod().apply { type = "trustly" }
        val applePayMethod = PaymentMethod().apply { type = "applepay" }

        response.paymentMethods = listOf(cardMethod, applePayMethod)

        if (isTrustlyIncluded)
            response.paymentMethods = response.paymentMethods.plus(trustlyyMethod)

        return response
    }

    private fun makeStoredPaymentMethods(isTrustlyIncluded: Boolean = true): PaymentMethodsResponse {
        val response = PaymentMethodsResponse()

        val cardMethod = StoredPaymentMethod().apply { type = "scheme" }
        val trustlyyMethod = StoredPaymentMethod().apply { type = "trustly" }
        val applePayMethod = StoredPaymentMethod().apply { type = "applepay" }

        val storedPaymentMethods : MutableList<StoredPaymentMethod>  = mutableListOf(cardMethod, applePayMethod)

        if (isTrustlyIncluded)
            storedPaymentMethods.add(trustlyyMethod)

        response.storedPaymentMethods = storedPaymentMethods

        return response
    }
}
