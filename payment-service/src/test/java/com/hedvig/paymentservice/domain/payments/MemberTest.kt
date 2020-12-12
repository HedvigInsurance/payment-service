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
import java.util.UUID
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
  fun `given two trustly accounts when a notification from the old account arrives, expect that the only the old account will be updated`() {
    val secondTrustlyAccountId = "secondTrustlyAccountId"

    fixture
      .given(
        MemberCreatedEvent(MEMBER_ID_ONE),
        makeTrustlyAccountCreatedEvent(MEMBER_ID_ONE),
        makeDirectDebitConnectedEvent(MEMBER_ID_ONE),
        makeTrustlyAccountCreatedEvent(MEMBER_ID_ONE, secondTrustlyAccountId),
        makeDirectDebitConnectedEvent(MEMBER_ID_ONE, secondTrustlyAccountId)
      )
      .`when`(
        UpdateTrustlyAccountCommand(
          memberId = MEMBER_ID_ONE,
          hedvigOrderId = HEDViG_ORDER_ID,
          accountId = TRUSTLY_ACCOUNT_ID,
          address = null,
          bank = null,
          city = null,
          clearingHouse = null,
          descriptor = null,
          directDebitMandateActive = false,
          lastDigits = null,
          name = null,
          personId = null,
          zipCode = null
        )
      )
      .expectSuccessfulHandlerExecution()
      .expectEvents(
        TrustlyAccountUpdatedEvent(
          memberId = MEMBER_ID_ONE,
          hedvigOrderId = HEDViG_ORDER_ID,
          trustlyAccountId = TRUSTLY_ACCOUNT_ID,
          address = null,
          bank = null,
          city = null,
          clearingHouse = null,
          descriptor = null,
          lastDigits = null,
          name = null,
          personId = null,
          zipCode = null
        ),
        DirectDebitDisconnectedEvent(
          MEMBER_ID_ONE,
          HEDViG_ORDER_ID.toString(),
          TRUSTLY_ACCOUNT_ID
        )
      )
      .expectState { member ->
        assertThat(member.trustlyAccounts.size).isEqualTo(2)
        assertThat(member.trustlyAccounts[TRUSTLY_ACCOUNT_ID]).isEqualTo(DirectDebitStatus.DISCONNECTED)
        assertThat(member.trustlyAccounts[secondTrustlyAccountId]).isEqualTo(DirectDebitStatus.CONNECTED)
      }
  }

  @Test
  fun `given three trustly accounts when a "disconnected" notification from the latest account arrives, expect that the only the latest account will be updated`() {
    val secondTrustlyAccountId = "secondTrustlyAccountId"
    val latestTrustlyAccountId = "thirdTrustlyAccountId"

    fixture
      .given(
        MemberCreatedEvent(MEMBER_ID_ONE),
        makeTrustlyAccountCreatedEvent(MEMBER_ID_ONE),
        makeDirectDebitConnectedEvent(MEMBER_ID_ONE),
        makeTrustlyAccountCreatedEvent(MEMBER_ID_ONE, secondTrustlyAccountId),
        makeDirectDebitConnectedEvent(MEMBER_ID_ONE, secondTrustlyAccountId),
        makeTrustlyAccountCreatedEvent(MEMBER_ID_ONE, latestTrustlyAccountId),
        makeDirectDebitConnectedEvent(MEMBER_ID_ONE, latestTrustlyAccountId)
      )
      .`when`(
        UpdateTrustlyAccountCommand(
          memberId = MEMBER_ID_ONE,
          hedvigOrderId = HEDViG_ORDER_ID,
          accountId = latestTrustlyAccountId,
          address = null,
          bank = null,
          city = null,
          clearingHouse = null,
          descriptor = null,
          directDebitMandateActive = false,
          lastDigits = null,
          name = null,
          personId = null,
          zipCode = null
        )
      )
      .expectSuccessfulHandlerExecution()
      .expectEvents(
        TrustlyAccountUpdatedEvent(
          memberId = MEMBER_ID_ONE,
          hedvigOrderId = HEDViG_ORDER_ID,
          trustlyAccountId = latestTrustlyAccountId,
          address = null,
          bank = null,
          city = null,
          clearingHouse = null,
          descriptor = null,
          lastDigits = null,
          name = null,
          personId = null,
          zipCode = null
        ),
        DirectDebitDisconnectedEvent(
          MEMBER_ID_ONE,
          HEDViG_ORDER_ID.toString(),
          latestTrustlyAccountId
        )
      )
      .expectState { member ->
        assertThat(member.trustlyAccounts.size).isEqualTo(3)
        assertThat(member.trustlyAccounts[TRUSTLY_ACCOUNT_ID]).isEqualTo(DirectDebitStatus.CONNECTED)
        assertThat(member.trustlyAccounts[secondTrustlyAccountId]).isEqualTo(DirectDebitStatus.CONNECTED)
        assertThat(member.trustlyAccounts[latestTrustlyAccountId]).isEqualTo(DirectDebitStatus.DISCONNECTED)
        assertThat(member.latestTrustlyAccountId).isEqualTo(latestTrustlyAccountId)
        assertThat(member.trustlyAccounts[latestTrustlyAccountId]).isEqualTo(DirectDebitStatus.DISCONNECTED)
      }
  }

  @Test
  fun given_memberCreatedEventAndAdyenAccountCreatedEvent_when_CreateCharge_expect_ChargeCreationFailedEvent() {
    fixture

      .given(MemberCreatedEvent(MEMBER_ID_ONE), makeAdyenAccountCreated(MEMBER_ID_ONE, AdyenAccountStatus.PENDING))
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

      .given(MemberCreatedEvent(MEMBER_ID_ONE), makeAdyenAccountCreated(MEMBER_ID_ONE, AdyenAccountStatus.AUTHORISED))
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

  private fun makeTrustlyAccountCreatedEvent(memberId: String, accountId: String = TRUSTLY_ACCOUNT_ID) =
    TrustlyAccountCreatedEvent(
      memberId = memberId,
      hedvigOrderId = HEDViG_ORDER_ID,
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
    trustlyAccountId: String = "trusttlyAccountId"
  ) =
    DirectDebitConnectedEvent(
      memberId = memberId,
      hedvigOrderId = "06467B87-3EED-4000-9887-2B4C6033FC05",
      trustlyAccountId = trustlyAccountId
    )

  private fun makeAdyenAccountCreated(memberId: String, status: AdyenAccountStatus) = AdyenAccountCreatedEvent(
    memberId = memberId,
    recurringDetailReference = RECURRING_DETAIL_REFERENCE,
    accountStatus = status
  )

  companion object {
    const val MEMBER_ID_ONE = "12345"
    val TRANSACTION_ID_ONE: UUID = UUID.fromString("4DC41766-803E-423F-B604-E7F7F8CE5FD7")
    val HEDViG_ORDER_ID: UUID = UUID.fromString("06467B87-3EED-4000-9887-2B4C6033FC05")
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
