package com.hedvig.paymentservice.domain.payments

import com.hedvig.paymentservice.domain.payments.commands.CreateChargeCommand
import com.hedvig.paymentservice.domain.payments.commands.CreatePayoutCommand
import com.hedvig.paymentservice.domain.payments.commands.SelectedPayoutDetails
import com.hedvig.paymentservice.domain.payments.commands.UpdateTrustlyAccountCommand
import com.hedvig.paymentservice.domain.payments.enums.AdyenAccountStatus
import com.hedvig.paymentservice.domain.payments.enums.Carrier
import com.hedvig.paymentservice.domain.payments.enums.PayinProvider
import com.hedvig.paymentservice.domain.payments.events.AdyenAccountCreatedEvent
import com.hedvig.paymentservice.domain.payments.events.ChargeCreatedEvent
import com.hedvig.paymentservice.domain.payments.events.ChargeCreationFailedEvent
import com.hedvig.paymentservice.domain.payments.events.DirectDebitConnectedEvent
import com.hedvig.paymentservice.domain.payments.events.DirectDebitDisconnectedEvent
import com.hedvig.paymentservice.domain.payments.events.MemberCreatedEvent
import com.hedvig.paymentservice.domain.payments.events.PayoutCreatedEvent
import com.hedvig.paymentservice.domain.payments.events.PayoutCreationFailedEvent
import com.hedvig.paymentservice.domain.payments.events.PayoutDetails
import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountCreatedEvent
import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountUpdatedEvent
import com.hedvig.paymentservice.serviceIntergration.productPricing.ProductPricingService
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.ContractMarketInfo
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.Market
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.money.Monetary
import javax.money.MonetaryAmount
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.test.aggregate.AggregateTestFixture
import org.javamoney.moneta.Money
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
class MemberTest {
    lateinit var fixture: AggregateTestFixture<Member>

    @MockkBean
    lateinit var productPricingService: ProductPricingService

    @Before
    fun setUp() {
        fixture = AggregateTestFixture(Member::class.java)
        fixture.registerInjectableResource(productPricingService)

        every { productPricingService.getContractMarketInfo(any()) } returns ContractMarketInfo(
            Market.NORWAY,
            Monetary.getCurrency("NOK")
        )
    }

    @Test
    fun given_memberCreatedEvent_when_CreateChargeWithDifferentCurrencyFromContract_expect_ChargeCreationFailedEvent() {
        every { productPricingService.getContractMarketInfo(any()) } returns ContractMarketInfo(
            Market.NORWAY,
            Monetary.getCurrency("SEK")
        )

        fixture
            .given(MemberCreatedEvent(MEMBER_ID_ONE))
            .`when`(
                makeCreateChargeCommand()
            )
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                makeChargeCreationFailedEvent(CURRENCY_MISMATCH)
            )
    }

    @Test
    fun given_memberCreatedEvent_when_CreateCharge_expect_ChargeCreationFailedEvent() {
        fixture

            .given(MemberCreatedEvent(MEMBER_ID_ONE))
            .`when`(
                makeCreateChargeCommand()
            )
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                makeChargeCreationFailedEvent(NO_PAYIN_METHOD_FOUND_MESSAGE)
            )
    }

    @Test
    fun given_memberCreatedEventAndTrustlyAccountCreatedEvent_when_CreateCharge_expect_ChargeCreationFailedEvent() {
        fixture

            .given(MemberCreatedEvent(MEMBER_ID_ONE), makeTrustlyAccountCreatedEvent(MEMBER_ID_ONE))
            .`when`(
                makeCreateChargeCommand()
            )
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                makeChargeCreationFailedEvent()
            )
    }

    @Test
    fun given_memberCreatedEventAndTrustlyAccountCreatedEventAndDirectDebitConnectedEvent_when_CreateCharge_expect_ChargeCreatedEvent() {
        fixture
            .given(
                MemberCreatedEvent(MEMBER_ID_ONE),
                makeTrustlyAccountCreatedEvent(MEMBER_ID_ONE),
                makeDirectDebitConnectedEvent(MEMBER_ID_ONE)
            )
            .`when`(
                makeCreateChargeCommand()
            )
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                makeChargeCreatedEvent()
            )
    }

    @Test
    fun given_memberCreatedEventAndTrustlyAccountCreatedEventAndDirectDebitConnectedEvent_when_CreateChargeWithNegativeAmount_expect_Explosion() {
        fixture
            .given(
                MemberCreatedEvent(MEMBER_ID_ONE),
                makeTrustlyAccountCreatedEvent(MEMBER_ID_ONE),
                makeDirectDebitConnectedEvent(MEMBER_ID_ONE)
            )
            .`when`(
                makeCreateChargeCommand().copy(amount = AMOUNT.negate())
            )
            .expectException(IllegalArgumentException::class.java)
            .expectNoEvents()
    }

    @Test
    fun given_memberCreatedEventAndTrustlyAccountCreatedEventAndDirectDebitConnectedEvent_when_CreatePayout_expect_PayoutCreatedEvent() {
        fixture
            .given(
                MemberCreatedEvent(MEMBER_ID_ONE),
                makeTrustlyAccountCreatedEvent(MEMBER_ID_ONE),
                makeDirectDebitConnectedEvent(MEMBER_ID_ONE)
            )
            .`when`(
                makeCreatePayoutCommand()
            )
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                makePayoutCreatedEvent()
            )
    }

    @Test
    fun given_memberCreatedEventAndTrustlyAccountCreatedEventAndDirectDebitConnectedEvent_when_CreatePayoutWithNegativeAmount_expect_Explosion() {
        fixture
            .given(
                MemberCreatedEvent(MEMBER_ID_ONE),
                makeTrustlyAccountCreatedEvent(MEMBER_ID_ONE),
                makeDirectDebitConnectedEvent(MEMBER_ID_ONE)
            )
            .`when`(
                makeCreatePayoutCommand().copy(amount = AMOUNT.negate())
            )
            .expectException(IllegalArgumentException::class.java)
            .expectNoEvents()
    }

    @Test
    fun given_memberCreatedEventAndTrustlyAccountCreatedEventAndDirectDebitConnectedEvent_when_CreateClaimPayoutWithoutCarrier_expect_PayoutCreationFailedEvent() {
        fixture
            .given(
                MemberCreatedEvent(MEMBER_ID_ONE),
                makeTrustlyAccountCreatedEvent(MEMBER_ID_ONE),
                makeDirectDebitConnectedEvent(MEMBER_ID_ONE)
            )
            .`when`(
                makeCreatePayoutCommand().copy(category = TransactionCategory.CLAIM, carrier = null)
            )
            .expectException(IllegalArgumentException::class.java)
    }

    @Test
    fun given_memberCreatedEventAndTrustlyAccountCreatedEventAndDirectDebitConnectedEvent_when_CreateClaimPayoutWithCarrier_expect_PayoutCreatedEvent() {
        fixture
            .given(
                MemberCreatedEvent(MEMBER_ID_ONE),
                makeTrustlyAccountCreatedEvent(MEMBER_ID_ONE),
                makeDirectDebitConnectedEvent(MEMBER_ID_ONE)
            )
            .`when`(
                makeCreatePayoutCommand().copy(category = TransactionCategory.CLAIM, carrier = Carrier.HDI)
            )
            .expectEvents(
                makePayoutCreatedEvent().copy(category = TransactionCategory.CLAIM, carrier = Carrier.HDI)
            )
    }

    @Test
    fun given_memberCreatedEventAndAdyenAccountCreatedEvent_when_CreateCharge_expect_ChargeCreationFailedEvent() {
        fixture
            .given(
                MemberCreatedEvent(MEMBER_ID_ONE),
                makeAdyenAccountCreated(MEMBER_ID_ONE, AdyenAccountStatus.PENDING)
            )
            .`when`(
                makeCreateChargeCommand()
            )
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                makeChargeCreationFailedEvent(ADYEN_NOT_AUTHORISED)
            )
    }

    @Test
    fun given_memberCreatedEventAndAdyenAccountCreatedEvent_when_CreateCharge_expect_ChargeCreatedEvent() {
        fixture
            .given(
                MemberCreatedEvent(MEMBER_ID_ONE),
                makeAdyenAccountCreated(MEMBER_ID_ONE, AdyenAccountStatus.AUTHORISED)
            )
            .`when`(
                makeCreateChargeCommand()
            )
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                makeChargeCreatedEvent(
                    payinProvider = PayinProvider.ADYEN,
                    providerId = RECURRING_DETAIL_REFERENCE
                )
            )
    }

    @Test
    fun `given two trustly accounts when a notification from the old account arrives, expect that only the old account will be updated`() {
        fixture
            .given(
                MemberCreatedEvent(MEMBER_ID_ONE),
                makeTrustlyAccountCreatedEvent(MEMBER_ID_ONE, TRUSTLY_ACCOUNT_ID_ONE, HEDVIG_ORDER_ID_ONE),
                makeDirectDebitConnectedEvent(MEMBER_ID_ONE, TRUSTLY_ACCOUNT_ID_ONE, HEDVIG_ORDER_ID_ONE),
                makeTrustlyAccountCreatedEvent(MEMBER_ID_ONE, TRUSTLY_ACCOUNT_ID_TWO, HEDVIG_ORDER_ID_TWO),
                makeDirectDebitConnectedEvent(MEMBER_ID_ONE, TRUSTLY_ACCOUNT_ID_TWO, HEDVIG_ORDER_ID_TWO)
            )
            .`when`(
                makeUpdateTrustlyAccountCommand(MEMBER_ID_ONE, TRUSTLY_ACCOUNT_ID_ONE, HEDVIG_ORDER_ID_ONE, false)
            )
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                makeTrustlyAccountUpdatedEvent(MEMBER_ID_ONE, TRUSTLY_ACCOUNT_ID_ONE, HEDVIG_ORDER_ID_ONE),
                DirectDebitDisconnectedEvent(
                    MEMBER_ID_ONE,
                    HEDVIG_ORDER_ID_ONE.toString(),
                    TRUSTLY_ACCOUNT_ID_ONE
                )
            )
            .expectState { member ->
                assertThat(member.directDebitAccountOrders.size).isEqualTo(2)
                assertThat(
                    member.directDebitAccountOrders
                        .first { it.hedvigOrderId == HEDVIG_ORDER_ID_ONE }
                        .account
                        .directDebitStatus
                ).isEqualTo(
                    DirectDebitStatus.DISCONNECTED
                )
                assertThat(
                    member.directDebitAccountOrders
                        .first { it.hedvigOrderId == HEDVIG_ORDER_ID_ONE }
                        .account
                        .accountId
                ).isEqualTo(
                    TRUSTLY_ACCOUNT_ID_ONE
                )
                assertThat(
                    member.directDebitAccountOrders
                        .first { it.hedvigOrderId == HEDVIG_ORDER_ID_TWO }
                        .account
                        .directDebitStatus
                ).isEqualTo(
                    DirectDebitStatus.CONNECTED
                )
                assertThat(
                    member.directDebitAccountOrders
                        .first { it.hedvigOrderId == HEDVIG_ORDER_ID_TWO }
                        .account
                        .accountId
                ).isEqualTo(
                    TRUSTLY_ACCOUNT_ID_TWO
                )
            }
    }

    @Test
    fun `given one connected trustly account when a notification from a different account with a new orderId arrives, expect the new account will be connected`() {
        fixture
            .given(
                MemberCreatedEvent(MEMBER_ID_ONE),
                makeTrustlyAccountCreatedEvent(MEMBER_ID_ONE, TRUSTLY_ACCOUNT_ID_ONE, HEDVIG_ORDER_ID_ONE),
                makeDirectDebitConnectedEvent(MEMBER_ID_ONE, TRUSTLY_ACCOUNT_ID_ONE, HEDVIG_ORDER_ID_ONE)
            )
            .`when`(
                makeUpdateTrustlyAccountCommand(MEMBER_ID_ONE, TRUSTLY_ACCOUNT_ID_TWO, HEDVIG_ORDER_ID_TWO)
            )
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                makeTrustlyAccountCreatedEvent(
                    MEMBER_ID_ONE,
                    TRUSTLY_ACCOUNT_ID_TWO,
                    HEDVIG_ORDER_ID_TWO
                ),
                DirectDebitConnectedEvent(
                    MEMBER_ID_ONE,
                    HEDVIG_ORDER_ID_TWO.toString(),
                    TRUSTLY_ACCOUNT_ID_TWO
                )
            )
            .expectState { member ->
                assertThat(member.directDebitAccountOrders.size).isEqualTo(2)
                assertThat(
                    member.directDebitAccountOrders
                        .first { it.hedvigOrderId == HEDVIG_ORDER_ID_TWO }
                        .account
                        .directDebitStatus
                ).isEqualTo(
                    DirectDebitStatus.CONNECTED
                )
                assertThat(
                    member.directDebitAccountOrders
                        .first { it.hedvigOrderId == HEDVIG_ORDER_ID_TWO }
                        .account
                        .accountId
                ).isEqualTo(
                    TRUSTLY_ACCOUNT_ID_TWO
                )
            }
    }

    @Test
    fun `given two trustly accounts when a charge arrives, expect the latest account will be charged`() {
        fixture
            .given(
                MemberCreatedEvent(MEMBER_ID_ONE),
                makeTrustlyAccountCreatedEvent(MEMBER_ID_ONE, TRUSTLY_ACCOUNT_ID_ONE, HEDVIG_ORDER_ID_ONE),
                makeDirectDebitConnectedEvent(MEMBER_ID_ONE, TRUSTLY_ACCOUNT_ID_ONE, HEDVIG_ORDER_ID_ONE),
                makeTrustlyAccountCreatedEvent(MEMBER_ID_ONE, TRUSTLY_ACCOUNT_ID_TWO, HEDVIG_ORDER_ID_TWO),
                makeDirectDebitConnectedEvent(MEMBER_ID_ONE, TRUSTLY_ACCOUNT_ID_TWO, HEDVIG_ORDER_ID_TWO)
            )
            .`when`(
                makeCreateChargeCommand()
            )
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                makeChargeCreatedEvent(
                    providerId = TRUSTLY_ACCOUNT_ID_TWO
                )
            )
    }

    @Test
    fun `given two trustly accounts and the latest is disconnected, when a charge arrives, expect the charge will fail`() {
        fixture
            .given(
                MemberCreatedEvent(MEMBER_ID_ONE),
                makeTrustlyAccountCreatedEvent(MEMBER_ID_ONE, TRUSTLY_ACCOUNT_ID_ONE, HEDVIG_ORDER_ID_ONE),
                makeDirectDebitConnectedEvent(MEMBER_ID_ONE, TRUSTLY_ACCOUNT_ID_ONE, HEDVIG_ORDER_ID_ONE),
                makeTrustlyAccountCreatedEvent(MEMBER_ID_ONE, TRUSTLY_ACCOUNT_ID_TWO, HEDVIG_ORDER_ID_TWO),
                makeDirectDebitDisConnectedEvent(MEMBER_ID_ONE, TRUSTLY_ACCOUNT_ID_TWO, HEDVIG_ORDER_ID_TWO)
            )
            .`when`(
                makeCreateChargeCommand()
            )
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                makeChargeCreationFailedEvent()
            )
    }

    @Test
    fun `given two trustly accounts and the latest is only updated, when a charge arrives, expect the latest account will be charged`() {
        fixture
            .given(
                MemberCreatedEvent(MEMBER_ID_ONE),
                makeTrustlyAccountCreatedEvent(MEMBER_ID_ONE, TRUSTLY_ACCOUNT_ID_ONE, HEDVIG_ORDER_ID_ONE),
                makeDirectDebitConnectedEvent(MEMBER_ID_ONE, TRUSTLY_ACCOUNT_ID_ONE, HEDVIG_ORDER_ID_ONE),
                makeTrustlyAccountUpdatedEvent(MEMBER_ID_ONE, TRUSTLY_ACCOUNT_ID_TWO, HEDVIG_ORDER_ID_TWO),
                makeDirectDebitConnectedEvent(MEMBER_ID_ONE, TRUSTLY_ACCOUNT_ID_TWO, HEDVIG_ORDER_ID_TWO)
            )
            .`when`(
                makeCreateChargeCommand()
            )
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                makeChargeCreatedEvent(
                    providerId = TRUSTLY_ACCOUNT_ID_TWO
                )
            )
    }

    private fun makeTrustlyAccountCreatedEvent(
        memberId: String,
        accountId: String = TRUSTLY_ACCOUNT_ID_ONE,
        hedvigOrderId: UUID = HEDVIG_ORDER_ID_ONE
    ) =
        TrustlyAccountCreatedEvent(
            memberId = memberId,
            hedvigOrderId = hedvigOrderId,
            trustlyAccountId = accountId,
            address = null,
            bank = null,
            city = null,
            clearingHouse = null,
            descriptor = null,
            lastDigits = null,
            name = null,
            personId = null,
            zipCode = null
        )

    private fun makeTrustlyAccountUpdatedEvent(
        memberId: String,
        accountId: String = TRUSTLY_ACCOUNT_ID_ONE,
        hedvigOrderId: UUID = HEDVIG_ORDER_ID_ONE
    ) = TrustlyAccountUpdatedEvent(
        memberId = memberId,
        hedvigOrderId = hedvigOrderId,
        trustlyAccountId = accountId,
        address = null,
        bank = null,
        city = null,
        clearingHouse = null,
        descriptor = null,
        lastDigits = null,
        name = null,
        personId = null,
        zipCode = null
    )

    private fun makeDirectDebitConnectedEvent(
        memberId: String,
        trustlyAccountId: String = TRUSTLY_ACCOUNT_ID_ONE,
        hedvigOrderId: UUID = HEDVIG_ORDER_ID_ONE
    ) =
        DirectDebitConnectedEvent(
            memberId = memberId,
            hedvigOrderId = hedvigOrderId.toString(),
            trustlyAccountId = trustlyAccountId
        )

    private fun makeDirectDebitDisConnectedEvent(
        memberId: String,
        trustlyAccountId: String = TRUSTLY_ACCOUNT_ID_ONE,
        hedvigOrderId: UUID = HEDVIG_ORDER_ID_ONE
    ) =
        DirectDebitDisconnectedEvent(
            memberId = memberId,
            hedvigOrderId = hedvigOrderId.toString(),
            trustlyAccountId = trustlyAccountId
        )

    private fun makeAdyenAccountCreated(memberId: String, status: AdyenAccountStatus) = AdyenAccountCreatedEvent(
        memberId = memberId,
        recurringDetailReference = RECURRING_DETAIL_REFERENCE,
        accountStatus = status
    )

    private fun makeUpdateTrustlyAccountCommand(
        memberId: String,
        accountId: String = TRUSTLY_ACCOUNT_ID_ONE,
        hedvigOrderId: UUID = HEDVIG_ORDER_ID_ONE,
        isConnected: Boolean = true
    ) = UpdateTrustlyAccountCommand(
        memberId = memberId,
        hedvigOrderId = hedvigOrderId,
        accountId = accountId,
        address = null,
        bank = null,
        city = null,
        clearingHouse = null,
        descriptor = null,
        directDebitMandateActive = isConnected,
        lastDigits = null,
        name = null,
        personId = null,
        zipCode = null
    )

    private fun makeCreateChargeCommand(
        transactionId: UUID = TRANSACTION_ID_ONE
    ) = CreateChargeCommand(
        memberId = MEMBER_ID_ONE,
        transactionId = transactionId,
        amount = AMOUNT,
        timestamp = NOW,
        email = EMAIL,
        createdBy = CREATED_BY
    )

    private fun makeCreatePayoutCommand(
        transactionId: UUID = TRANSACTION_ID_ONE
    ) = CreatePayoutCommand(
        memberId = MEMBER_ID_ONE,
        address = "Address",
        countryCode = null,
        dateOfBirth = LocalDate.of(1912, 12, 12),
        firstName = FIRST_NAME,
        lastName = LAST_NAME,
        transactionId = transactionId,
        amount = AMOUNT,
        timestamp = NOW,
        email = EMAIL,
        category = TransactionCategory.CLAIM,
        referenceId = null,
        note = null,
        handler = null,
        carrier = Carrier.HDI,
        selectedPayoutDetails = SelectedPayoutDetails.NotSelected
    )

    private fun makeChargeCreatedEvent(
        payinProvider: PayinProvider = PayinProvider.TRUSTLY,
        providerId: String = TRUSTLY_ACCOUNT_ID_ONE
    ) = ChargeCreatedEvent(
        memberId = MEMBER_ID_ONE,
        transactionId = TRANSACTION_ID_ONE,
        amount = AMOUNT,
        timestamp = NOW,
        providerId = providerId,
        provider = payinProvider,
        email = EMAIL,
        createdBy = CREATED_BY
    )

    private fun makePayoutCreatedEvent(
        transactionId: UUID = TRANSACTION_ID_ONE,
        trustlyAccountId: String? = TRUSTLY_ACCOUNT_ID_ONE,
        adyenShopperReference: String? = null
    ) = PayoutCreatedEvent(
        memberId = MEMBER_ID_ONE,
        address = "Address",
        countryCode = null,
        dateOfBirth = LocalDate.of(1912, 12, 12),
        firstName = FIRST_NAME,
        lastName = LAST_NAME,
        transactionId = transactionId,
        amount = AMOUNT,
        timestamp = NOW,
        email = EMAIL,
        category = TransactionCategory.CLAIM,
        referenceId = null,
        note = null,
        carrier = Carrier.HDI,
        payoutDetails = PayoutDetails.Trustly(trustlyAccountId!!)
    )

    private fun makeChargeCreationFailedEvent(
        reason: String = DIRECT_DEBIT_NOT_CONNECTED
    ) = ChargeCreationFailedEvent(
        memberId = MEMBER_ID_ONE,
        transactionId = TRANSACTION_ID_ONE,
        amount = AMOUNT,
        timestamp = NOW,
        reason = reason
    )

    private fun makePayoutCreationFailedEvent() = PayoutCreationFailedEvent(
        memberId = MEMBER_ID_ONE,
        transactionId = TRANSACTION_ID_ONE,
        amount = AMOUNT,
        timestamp = NOW
    )

    companion object {
        const val MEMBER_ID_ONE = "12345"
        const val FIRST_NAME = "Tolvan"
        const val LAST_NAME = "Tolvansson"
        val TRANSACTION_ID_ONE: UUID = UUID.fromString("4DC41766-803E-423F-B604-E7F7F8CE5FD7")
        val HEDVIG_ORDER_ID_ONE: UUID = UUID.fromString("06467B87-3EED-4000-9887-2B4C6033FC05")
        val HEDVIG_ORDER_ID_TWO: UUID = UUID.fromString("DE58DE3C-C7FD-456D-A3F3-1CD840D8B505")
        val AMOUNT: MonetaryAmount = Money.of(1234, "NOK")
        val NOW: Instant = Instant.now()
        const val EMAIL: String = "test@hedvig.com"
        const val CREATED_BY: String = "hedvig"
        const val NO_PAYIN_METHOD_FOUND_MESSAGE = "no payin method found"
        const val DIRECT_DEBIT_NOT_CONNECTED = "direct debit mandate not received in Trustly"
        const val TRUSTLY_ACCOUNT_ID_ONE = "trustlyAccountId"
        const val TRUSTLY_ACCOUNT_ID_TWO = "secondTrustlyAccountId"
        const val RECURRING_DETAIL_REFERENCE = "recurringDetailReference"
        const val ADYEN_NOT_AUTHORISED = "adyen recurring is not authorised"
        const val CURRENCY_MISMATCH = "currency mismatch"
    }
}
