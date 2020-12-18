package com.hedvig.paymentservice.domain.payments

import com.hedvig.paymentservice.domain.payments.commands.CreateChargeCommand
import com.hedvig.paymentservice.domain.payments.commands.UpdateTrustlyAccountCommand
import com.hedvig.paymentservice.domain.payments.enums.AdyenAccountStatus
import com.hedvig.paymentservice.domain.payments.enums.PayinProvider
import com.hedvig.paymentservice.domain.payments.events.AdyenAccountCreatedEvent
import com.hedvig.paymentservice.domain.payments.events.ChargeCreatedEvent
import com.hedvig.paymentservice.domain.payments.events.ChargeCreationFailedEvent
import com.hedvig.paymentservice.domain.payments.events.DirectDebitConnectedEvent
import com.hedvig.paymentservice.domain.payments.events.DirectDebitDisconnectedEvent
import com.hedvig.paymentservice.domain.payments.events.MemberCreatedEvent
import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountCreatedEvent
import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountUpdatedEvent
import com.hedvig.paymentservice.serviceIntergration.productPricing.ProductPricingService
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.ContractMarketInfo
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.Market
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.test.aggregate.AggregateTestFixture
import org.javamoney.moneta.Money
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringRunner
import java.time.Instant
import java.util.*
import javax.money.Monetary
import javax.money.MonetaryAmount

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
                CreateChargeCommand(
                    memberId = MEMBER_ID_ONE,
                    transactionId = TRANSACTION_ID_ONE,
                    amount = AMOUNT,
                    timestamp = NOW,
                    email = EMAIL,
                    createdBy = CREATED_BY
                )
            )
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                ChargeCreationFailedEvent(
                    memberId = MEMBER_ID_ONE,
                    transactionId = TRANSACTION_ID_ONE,
                    amount = AMOUNT,
                    timestamp = NOW,
                    reason = CURRENCY_MISMATCH
                )
            )
    }

    @Test
    fun given_memberCreatedEvent_when_CreateCharge_expect_ChargeCreationFailedEvent() {
        fixture

            .given(MemberCreatedEvent(MEMBER_ID_ONE))
            .`when`(
                CreateChargeCommand(
                    memberId = MEMBER_ID_ONE,
                    transactionId = TRANSACTION_ID_ONE,
                    amount = AMOUNT,
                    timestamp = NOW,
                    email = EMAIL,
                    createdBy = CREATED_BY
                )
            )
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                ChargeCreationFailedEvent(
                    memberId = MEMBER_ID_ONE,
                    transactionId = TRANSACTION_ID_ONE,
                    amount = AMOUNT,
                    timestamp = NOW,
                    reason = NO_PAYIN_METHOD_FOUND_MESSAGE
                )
            )
    }

    @Test
    fun given_memberCreatedEventAndTrustlyAccountCreatedEvent_when_CreateCharge_expect_ChargeCreationFailedEvent() {
        fixture

            .given(MemberCreatedEvent(MEMBER_ID_ONE), makeTrustlyAccountCreatedEvent(MEMBER_ID_ONE))
            .`when`(
                CreateChargeCommand(
                    memberId = MEMBER_ID_ONE,
                    transactionId = TRANSACTION_ID_ONE,
                    amount = AMOUNT,
                    timestamp = NOW,
                    email = EMAIL,
                    createdBy = CREATED_BY
                )
            )
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                ChargeCreationFailedEvent(
                    memberId = MEMBER_ID_ONE,
                    transactionId = TRANSACTION_ID_ONE,
                    amount = AMOUNT,
                    timestamp = NOW,
                    reason = DIRECT_DEBIT_NOT_CONNECTED
                )
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
                CreateChargeCommand(
                    memberId = MEMBER_ID_ONE,
                    transactionId = TRANSACTION_ID_ONE,
                    amount = AMOUNT,
                    timestamp = NOW,
                    email = EMAIL,
                    createdBy = CREATED_BY
                )
            )
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                ChargeCreatedEvent(
                    memberId = MEMBER_ID_ONE,
                    transactionId = TRANSACTION_ID_ONE,
                    amount = AMOUNT,
                    timestamp = NOW,
                    providerId = TRUSTLY_ACCOUNT_ID,
                    provider = PayinProvider.TRUSTLY,
                    email = EMAIL,
                    createdBy = CREATED_BY
                )
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
                CreateChargeCommand(
                    memberId = MEMBER_ID_ONE,
                    transactionId = TRANSACTION_ID_ONE,
                    amount = AMOUNT,
                    timestamp = NOW,
                    email = EMAIL,
                    createdBy = CREATED_BY
                )
            )
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                ChargeCreationFailedEvent(
                    memberId = MEMBER_ID_ONE,
                    transactionId = TRANSACTION_ID_ONE,
                    amount = AMOUNT,
                    timestamp = NOW,
                    reason = ADYEN_NOT_AUTHORISED
                )
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
                CreateChargeCommand(
                    memberId = MEMBER_ID_ONE,
                    transactionId = TRANSACTION_ID_ONE,
                    amount = AMOUNT,
                    timestamp = NOW,
                    email = EMAIL,
                    createdBy = CREATED_BY
                )
            )
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                ChargeCreatedEvent(
                    memberId = MEMBER_ID_ONE,
                    transactionId = TRANSACTION_ID_ONE,
                    amount = AMOUNT,
                    timestamp = NOW,
                    providerId = RECURRING_DETAIL_REFERENCE,
                    provider = PayinProvider.ADYEN,
                    email = EMAIL,
                    createdBy = CREATED_BY
                )
            )
    }

    @Test
    fun `given two trustly accounts when a notification from the old account arrives, expect that only the old account will be updated`() {
        val secondTrustlyAccountId = "secondTrustlyAccountId"

        fixture
            .given(
                MemberCreatedEvent(MEMBER_ID_ONE),
                makeTrustlyAccountCreatedEvent(MEMBER_ID_ONE, TRUSTLY_ACCOUNT_ID, HEDVIG_ORDER_ID_ONE),
                makeDirectDebitConnectedEvent(MEMBER_ID_ONE, TRUSTLY_ACCOUNT_ID, HEDVIG_ORDER_ID_ONE),
                makeTrustlyAccountCreatedEvent(MEMBER_ID_ONE, secondTrustlyAccountId, HEDVIG_ORDER_ID_TWO),
                makeDirectDebitConnectedEvent(MEMBER_ID_ONE, secondTrustlyAccountId, HEDVIG_ORDER_ID_TWO)
            )
            .`when`(
                makeUpdateTrustlyAccountCommand(MEMBER_ID_ONE, TRUSTLY_ACCOUNT_ID, HEDVIG_ORDER_ID_ONE, false)
            )
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                makeTrustlyAccountUpdatedEvent(MEMBER_ID_ONE, TRUSTLY_ACCOUNT_ID, HEDVIG_ORDER_ID_ONE),
                DirectDebitDisconnectedEvent(
                    MEMBER_ID_ONE,
                    HEDVIG_ORDER_ID_ONE.toString(),
                    TRUSTLY_ACCOUNT_ID
                )
            )
            .expectState { member ->
                assertThat(member.directDebitAccountOrders.size).isEqualTo(2)
                assertThat(member.directDebitAccountOrders
                    .first { it.hedvigOrderId == HEDVIG_ORDER_ID_ONE }
                    .account
                    .directDebitStatus
                ).isEqualTo(
                    DirectDebitStatus.DISCONNECTED
                )
                assertThat(member.directDebitAccountOrders
                    .first { it.hedvigOrderId == HEDVIG_ORDER_ID_ONE }
                    .account
                    .accountId
                ).isEqualTo(
                    TRUSTLY_ACCOUNT_ID
                )
                assertThat(member.directDebitAccountOrders
                    .first { it.hedvigOrderId == HEDVIG_ORDER_ID_TWO }
                    .account
                    .directDebitStatus
                ).isEqualTo(
                    DirectDebitStatus.CONNECTED
                )
                assertThat(member.directDebitAccountOrders
                    .first { it.hedvigOrderId == HEDVIG_ORDER_ID_TWO }
                    .account
                    .accountId
                ).isEqualTo(
                    secondTrustlyAccountId
                )
            }
    }

    @Test
    fun `given one connected trustly account when a notification from a different account with a new orderId arrives, expect the new account will be connected`() {
        val secondTrustlyAccountId = "secondTrustlyAccountId"

        fixture
            .given(
                MemberCreatedEvent(MEMBER_ID_ONE),
                makeTrustlyAccountCreatedEvent(MEMBER_ID_ONE, TRUSTLY_ACCOUNT_ID, HEDVIG_ORDER_ID_ONE),
                makeDirectDebitConnectedEvent(MEMBER_ID_ONE, TRUSTLY_ACCOUNT_ID, HEDVIG_ORDER_ID_ONE)
            )
            .`when`(
                makeUpdateTrustlyAccountCommand(MEMBER_ID_ONE, secondTrustlyAccountId, HEDVIG_ORDER_ID_TWO)
            )
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                makeTrustlyAccountCreatedEvent(
                    MEMBER_ID_ONE,
                    secondTrustlyAccountId,
                    HEDVIG_ORDER_ID_TWO
                ),
                DirectDebitConnectedEvent(
                    MEMBER_ID_ONE,
                    HEDVIG_ORDER_ID_TWO.toString(),
                    secondTrustlyAccountId
                )
            )
            .expectState { member ->
                assertThat(member.directDebitAccountOrders.size).isEqualTo(2)
                assertThat(member.directDebitAccountOrders
                    .first { it.hedvigOrderId == HEDVIG_ORDER_ID_TWO }
                    .account
                    .directDebitStatus
                ).isEqualTo(
                    DirectDebitStatus.CONNECTED
                )
                assertThat(member.directDebitAccountOrders
                    .first { it.hedvigOrderId == HEDVIG_ORDER_ID_TWO }
                    .account
                    .accountId
                ).isEqualTo(
                    secondTrustlyAccountId
                )
            }
    }

    // Happy flow with charge command which will pick the latest account
    //TrustlyUpdatedEvent - 70 people which will pick the latest account

    private fun makeTrustlyAccountCreatedEvent(
        memberId: String,
        accountId: String = TRUSTLY_ACCOUNT_ID,
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
        accountId: String = TRUSTLY_ACCOUNT_ID,
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
        trustlyAccountId: String = "trusttlyAccountId",
        hedvigOrderId: UUID = HEDVIG_ORDER_ID_ONE
    ) =
        DirectDebitConnectedEvent(
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
        accountId: String = TRUSTLY_ACCOUNT_ID,
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

    companion object {
        const val MEMBER_ID_ONE = "12345"
        val TRANSACTION_ID_ONE: UUID = UUID.fromString("4DC41766-803E-423F-B604-E7F7F8CE5FD7")
        val HEDVIG_ORDER_ID_ONE: UUID = UUID.fromString("06467B87-3EED-4000-9887-2B4C6033FC05")
        val HEDVIG_ORDER_ID_TWO: UUID = UUID.fromString("DE58DE3C-C7FD-456D-A3F3-1CD840D8B505")
        val HEDVIG_ORDER_ID_THREE: UUID = UUID.fromString("C28A27DE-5839-4C0F-AD09-F94B69A2248A")
        val AMOUNT: MonetaryAmount = Money.of(1234, "NOK")
        val NOW: Instant = Instant.now()
        const val EMAIL: String = "test@hedvig.com"
        const val CREATED_BY: String = "hedvig"
        const val NO_PAYIN_METHOD_FOUND_MESSAGE = "no payin method found"
        const val DIRECT_DEBIT_NOT_CONNECTED = "direct debit mandate not received in Trustly"
        const val TRUSTLY_ACCOUNT_ID = "trusttlyAccountId"
        const val RECURRING_DETAIL_REFERENCE = "recurringDetailReference"
        const val ADYEN_NOT_AUTHORISED = "adyen recurring is not authorised"
        const val CURRENCY_MISMATCH = "currency mismatch"

    }
}
