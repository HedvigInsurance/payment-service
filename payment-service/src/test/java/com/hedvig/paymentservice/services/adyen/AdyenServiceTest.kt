package com.hedvig.paymentservice.services.adyen

import com.adyen.model.checkout.PaymentMethod
import com.adyen.model.checkout.PaymentMethodsResponse
import com.adyen.service.Checkout
import com.adyen.service.Payout
import com.hedvig.paymentservice.common.UUIDGenerator
import com.hedvig.paymentservice.domain.adyenTokenRegistration.commands.AuthoriseAdyenTokenRegistrationFromNotificationCommand
import com.hedvig.paymentservice.domain.adyenTokenRegistration.commands.CancelAdyenTokenFromNotificationRegistrationCommand
import com.hedvig.paymentservice.domain.adyenTokenRegistration.enums.AdyenTokenRegistrationStatus
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceiveAdyenTransactionUnsuccessfulRetryResponseCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceiveAuthorisationAdyenTransactionCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceiveCancellationResponseAdyenTransactionCommand
import com.hedvig.paymentservice.graphQl.types.PayoutMethodStatus
import com.hedvig.paymentservice.query.adyenAccount.MemberAdyenAccountRepository
import com.hedvig.paymentservice.query.adyenTokenRegistration.entities.AdyenTokenRegistration
import com.hedvig.paymentservice.query.adyenTokenRegistration.entities.AdyenTokenRegistrationRepository
import com.hedvig.paymentservice.query.adyenTransaction.entities.AdyenPayoutTransactionRepository
import com.hedvig.paymentservice.query.adyenTransaction.entities.AdyenTransaction
import com.hedvig.paymentservice.query.adyenTransaction.entities.AdyenTransactionRepository
import com.hedvig.paymentservice.query.member.entities.MemberRepository
import com.hedvig.paymentservice.serviceIntergration.memberService.MemberService
import com.hedvig.paymentservice.services.adyen.dtos.AdyenMerchantInfo
import com.hedvig.paymentservice.services.adyen.extentions.NoMerchantAccountForMarket
import com.hedvig.paymentservice.services.adyen.util.AdyenMerchantPicker
import com.hedvig.paymentservice.web.dtos.adyen.NotificationRequestItem
import com.neovisionaries.i18n.CountryCode
import com.neovisionaries.i18n.CurrencyCode
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.commandhandling.gateway.CommandGateway
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringRunner
import java.math.BigDecimal
import java.util.*

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

    @MockkBean
    lateinit var memberAdyenAccountRepository: MemberAdyenAccountRepository

    lateinit var adyenService: AdyenService

    @Before
    fun setup() {
        adyenService = AdyenServiceImpl(
            adyenCheckout = adyenCheckout,
            adyenPayout = adyenPayout,
            adyenPayoutConfirmation = adyenPayout,
            memberRepository = memberRepository,
            uuidGenerator = uuidGenerator,
            memberService = memberService,
            commandGateway = commandGateway,
            tokenRegistrationRepository = adyenTokenRegistrationRepository,
            transactionRepository = adyenTransactionRepository,
            adyenPayoutTransactionRepository = adyenPayoutTransactionRepository,
            adyenMerchantPicker = adyenMerchantPicker,
            memberAdyenAccountRepository = memberAdyenAccountRepository,
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
    fun `expect a AuthoriseAdyenTokenRegistrationFromNotificationCommand to be dispatched when a notification for tokenization are being handled`() {
        val notification = makeNotificationRequestItem(isSuccessful = true)
        val tokenRegistration = makeAdyenTokenRegistration()

        every { adyenTransactionRepository.findById(any()) } returns Optional.empty()
        every { adyenTokenRegistrationRepository.findById(any()) } returns Optional.of(tokenRegistration)
        every { commandGateway.sendAndWait<AuthoriseAdyenTokenRegistrationFromNotificationCommand>(any()) } returns null

        adyenService.handleAuthorisationNotification(notification)

        verify(exactly = 1) { commandGateway.sendAndWait(ofType(AuthoriseAdyenTokenRegistrationFromNotificationCommand::class))  }
    }

    @Test
    fun `expect a CancelAdyenTokenFromNotificationRegistrationCommand to be dispatched when a notification for failed tokenization are being handled`() {
        val notification = makeNotificationRequestItem(isSuccessful = false)
        val tokenRegistration = makeAdyenTokenRegistration()

        every { adyenTransactionRepository.findById(any()) } returns Optional.empty()
        every { adyenTokenRegistrationRepository.findById(any()) } returns Optional.of(tokenRegistration)
        every { commandGateway.sendAndWait<CancelAdyenTokenFromNotificationRegistrationCommand>(any()) } returns null

        adyenService.handleAuthorisationNotification(notification)

        verify(exactly = 1) { commandGateway.sendAndWait(ofType(CancelAdyenTokenFromNotificationRegistrationCommand::class)) }
    }

    @Test
    fun `expect ReceiveAuthorisationAdyenTransactionCommand to be dispatched when a notification for successful payin are being handled`() {
        val notification = makeNotificationRequestItem(isSuccessful = true)

        every { adyenTransactionRepository.findById(any()) } returns Optional.of(makeAdyenTransaction())
        every { adyenTokenRegistrationRepository.findById(any()) } returns Optional.empty()
        every { commandGateway.sendAndWait<ReceiveAuthorisationAdyenTransactionCommand>(any()) } returns null

        adyenService.handleAuthorisationNotification(notification)

        verify(exactly = 1) { commandGateway.sendAndWait(ofType(ReceiveAuthorisationAdyenTransactionCommand::class)) }
    }

    @Test
    fun `expect ReceiveAdyenTransactionUnsuccessfulRetryResponseCommand to be dispatched when a notification for successful payin are being handled`() {
        val notification = makeNotificationRequestItem(isSuccessful = false, isAutoRescue = true)

        every { adyenTransactionRepository.findById(any()) } returns Optional.of(makeAdyenTransaction())
        every { adyenTokenRegistrationRepository.findById(any()) } returns Optional.empty()
        every { commandGateway.sendAndWait<ReceiveAdyenTransactionUnsuccessfulRetryResponseCommand>(any()) } returns null

        adyenService.handleAuthorisationNotification(notification)

        verify(exactly = 1) { commandGateway.sendAndWait(ofType(ReceiveAdyenTransactionUnsuccessfulRetryResponseCommand::class)) }
    }

    @Test
    fun `expect ReceiveCancellationResponseAdyenTransactionCommand to be dispatched when a notification for successful payin are being handled`() {
        val notification = makeNotificationRequestItem(isSuccessful = false, isAutoRescue = false)

        every { adyenTransactionRepository.findById(any()) } returns Optional.of(makeAdyenTransaction())
        every { adyenTokenRegistrationRepository.findById(any()) } returns Optional.empty()
        every { commandGateway.sendAndWait<ReceiveCancellationResponseAdyenTransactionCommand>(any()) } returns null

        adyenService.handleAuthorisationNotification(notification)

        verify(exactly = 1) { commandGateway.sendAndWait(ofType(ReceiveCancellationResponseAdyenTransactionCommand::class)) }
    }

    @Test
    fun`expect active when there is a authorized token for payouts`() {
        every { adyenMerchantPicker.getAdyenMerchantInfo(any()) } returns AdyenMerchantInfo(
            "account",
            CountryCode.NO,
            CurrencyCode.NOK
        )
        every { adyenTokenRegistrationRepository.findByMemberId(any()) } returns listOf(makeAdyenTokenRegistration())

        val status = adyenService.getLatestPayoutTokenRegistrationStatus("1234")

        assertThat(status).isEqualTo(PayoutMethodStatus.ACTIVE)
    }

    @Test
    fun`expect null when there is a no token for payouts and its from a country without Adyen`() {
        every { adyenMerchantPicker.getAdyenMerchantInfo(any()) } throws NoMerchantAccountForMarket(AdyenMerchantPicker.Market.SWEDEN)
        every { adyenTokenRegistrationRepository.findByMemberId(any()) } returns listOf()

        val status = adyenService.getLatestPayoutTokenRegistrationStatus("1234")

        assertThat(status).isEqualTo(null)
    }

    @Test
    fun`expect NEEDS_SETUP when there is a no token for payouts`() {
        every { adyenMerchantPicker.getAdyenMerchantInfo(any()) } returns AdyenMerchantInfo(
            "account",
            CountryCode.NO,
            CurrencyCode.NOK
        )
        every { adyenTokenRegistrationRepository.findByMemberId(any()) } returns listOf()

        val status = adyenService.getLatestPayoutTokenRegistrationStatus("1234")

        assertThat(status).isEqualTo(PayoutMethodStatus.NEEDS_SETUP)
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

    private fun makeNotificationRequestItem(
        isSuccessful: Boolean,
        merchantReference: String? = UUID.randomUUID().toString(),
            isAutoRescue: Boolean = false
    ) = NotificationRequestItem(
        amount = null,
        eventCode = "AUTHORISATION",
        eventDate = "2021-02-17",
        merchantAccountCode = "Hedvig",
        merchantReference = merchantReference,
        originalReference = "Original Reference",
        pspReference = "PSP Reference",
        reason = "reason",
        success = isSuccessful,
        paymentMethod = "TRUSTLY",
        operations = null,
        additionalData = if (isAutoRescue) mapOf("retry.rescueScheduled" to "true", "retry.rescueReference" to "something", "retry.orderAttemptNumber" to "2") else null
    )

    private fun makeAdyenTokenRegistration(status : AdyenTokenRegistrationStatus = AdyenTokenRegistrationStatus.AUTHORISED): AdyenTokenRegistration {
        val tokenRegistration = AdyenTokenRegistration()
        tokenRegistration.adyenTokenRegistrationId = UUID.fromString("CD076349-4454-432A-AD19-42C5C4A1396A")
        tokenRegistration.memberId = "MEMBER_ID"
        tokenRegistration.shopperReference = ""
        tokenRegistration.isForPayout = true
        tokenRegistration.tokenStatus = status

        return tokenRegistration
    }

    private fun makeAdyenTransaction() = AdyenTransaction().apply {
        transactionId = UUID.fromString("8C0A90BF-A8A4-4F2D-A68B-BA40B10C39FB");
        memberId = "1234";
        amount = BigDecimal.TEN;
        currency = "SEK"
    }
}
