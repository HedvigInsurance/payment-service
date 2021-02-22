package com.hedvig.paymentservice.services.memberPayinFilter

import com.hedvig.paymentservice.PaymentServiceTestConfiguration
import com.hedvig.paymentservice.domain.payments.DirectDebitStatus
import com.hedvig.paymentservice.domain.payments.enums.AdyenAccountStatus
import com.hedvig.paymentservice.query.adyenAccount.MemberAdyenAccount
import com.hedvig.paymentservice.query.adyenAccount.MemberAdyenAccountRepository
import com.hedvig.paymentservice.query.directDebit.DirectDebitAccountOrder
import com.hedvig.paymentservice.query.directDebit.DirectDebitAccountOrderRepository
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.Market
import com.hedvig.paymentservice.services.payinMethodFilter.MemberPayinMethodFilterService
import com.hedvig.paymentservice.services.payinMethodFilter.MemberPayinMethodFilterServiceImpl
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@DataJpaTest
@ContextConfiguration(classes = [PaymentServiceTestConfiguration::class])
class MemberPayinFilterServiceTest {
    @MockkBean
    lateinit var memberAdyenAccountRepository: MemberAdyenAccountRepository

    @Autowired
    lateinit var directDebitAccountOrderRepository: DirectDebitAccountOrderRepository

    private lateinit var classUnderTest: MemberPayinMethodFilterService

    @Before
    fun setup() {
        classUnderTest = MemberPayinMethodFilterServiceImpl(directDebitAccountOrderRepository, memberAdyenAccountRepository)
    }

    @Test
    fun `if market is Sweden only return memberIds if the latest direct debit account order is connected`() {
        val directDebitAccountOrderOne = buildDirectDebitAccountOrder(
            id = "123",
            trustlyAccountNumber = "222",
            directDebitStatus = DirectDebitStatus.CONNECTED
        )

        val directDebitAccountOrderTwo = buildDirectDebitAccountOrder(
            id = "234",
            trustlyAccountNumber = "accountOne",
            directDebitStatus = DirectDebitStatus.CONNECTED,
            createdAt = Instant.now().minus(2, ChronoUnit.DAYS)
        )

        val directDebitAccountOrderThree = buildDirectDebitAccountOrder(
            id = "234",
            trustlyAccountNumber = "accountTwo",
            directDebitStatus = DirectDebitStatus.DISCONNECTED,
            createdAt = Instant.now().minus(1, ChronoUnit.DAYS)
        )

        directDebitAccountOrderRepository.saveAll(
            listOf(
                directDebitAccountOrderOne,
                directDebitAccountOrderTwo,
                directDebitAccountOrderThree
            )
        )

        val result = classUnderTest.membersWithConnectedPayinMethodForMarket(
            listOf(
                "123", "234"
            ),
            Market.SWEDEN
        )

        assertThat(result.size).isEqualTo(1)
        assertThat(result[0]).isEqualTo("123")
    }

    @Test
    fun `if market is Sweden only return memberIds if the latest direct debit account order is connected and memberId is in list sent in`() {
        val directDebitAccountOrderOne = buildDirectDebitAccountOrder(
            id = "123",
            trustlyAccountNumber = "222",
            directDebitStatus = DirectDebitStatus.CONNECTED
        )

        val directDebitAccountOrderTwo = buildDirectDebitAccountOrder(
            id = "234",
            trustlyAccountNumber = "accountOne",
            directDebitStatus = DirectDebitStatus.CONNECTED,
            createdAt = Instant.now().minus(2, ChronoUnit.DAYS)
        )

        val directDebitAccountOrderThree = buildDirectDebitAccountOrder(
            id = "234",
            trustlyAccountNumber = "accountTwo",
            directDebitStatus = DirectDebitStatus.DISCONNECTED,
            createdAt = Instant.now().minus(1, ChronoUnit.DAYS)
        )

        directDebitAccountOrderRepository.saveAll(
            listOf(
                directDebitAccountOrderOne,
                directDebitAccountOrderTwo,
                directDebitAccountOrderThree
            )
        )

        val result = classUnderTest.membersWithConnectedPayinMethodForMarket(
            listOf(
                "234"
            ),
            Market.SWEDEN
        )

        assertThat(result.size).isEqualTo(0)
    }

    @Test
    fun `if market is Sweden and all latest direct debit account orders are disconnected return empty list`() {
        val directDebitAccountOrderOne = buildDirectDebitAccountOrder(
            id = "123",
            trustlyAccountNumber = "accountOne",
            directDebitStatus = DirectDebitStatus.DISCONNECTED,
            createdAt = Instant.now()
        )
        val directDebitAccountOrderTwo = buildDirectDebitAccountOrder(
            id = "234",
            trustlyAccountNumber = "accountTwo",
            directDebitStatus = DirectDebitStatus.CONNECTED,
            createdAt = Instant.now().minus(5, ChronoUnit.DAYS)
        )
        val directDebitAccountOrderThree = buildDirectDebitAccountOrder(
            id = "234",
            trustlyAccountNumber = "accountTwo",
            directDebitStatus = DirectDebitStatus.DISCONNECTED,
            createdAt = Instant.now().minus(1, ChronoUnit.DAYS)
        )

        directDebitAccountOrderRepository.saveAll(
            listOf(
                directDebitAccountOrderOne,
                directDebitAccountOrderTwo,
                directDebitAccountOrderThree
            )
        )

        val result = classUnderTest.membersWithConnectedPayinMethodForMarket(
            listOf("123", "234"),
            Market.SWEDEN
        )

        assertThat(result.size).isEqualTo(0)
    }

    @Test
    fun `if market is Sweden and all direct debit account orders are connected return the latest connected`() {
        val directDebitAccountOrderOne = buildDirectDebitAccountOrder(
            id = "234",
            trustlyAccountNumber = "account",
            directDebitStatus = DirectDebitStatus.CONNECTED,
            createdAt = Instant.now()
        )
        val directDebitAccountOrderTwo = buildDirectDebitAccountOrder(
            id = "234",
            trustlyAccountNumber = "accountTwo",
            directDebitStatus = DirectDebitStatus.CONNECTED,
            createdAt = Instant.now().minus(5, ChronoUnit.DAYS)
        )
        val directDebitAccountOrderThree = buildDirectDebitAccountOrder(
            id = "234",
            trustlyAccountNumber = "accountTwo",
            directDebitStatus = DirectDebitStatus.CONNECTED,
            createdAt = Instant.now().minus(1, ChronoUnit.DAYS)
        )

        directDebitAccountOrderRepository.saveAll(
            listOf(
                directDebitAccountOrderOne,
                directDebitAccountOrderTwo,
                directDebitAccountOrderThree
            )
        )

        val result = classUnderTest.membersWithConnectedPayinMethodForMarket(
            listOf("123", "234"),
            Market.SWEDEN
        )

        assertThat(result.size).isEqualTo(1)
        assertThat(result[0]).isEqualTo("234")
    }

    @Test
    fun `if market is Norway and one member has Adyen connected and one member does not have Adyen connected only return member with Adyen connected`() {
        every { memberAdyenAccountRepository.findAllByMemberIdIn(listOf("123", "234")) } returns
            listOf(
                buildAdyenAccount(),
                buildAdyenAccount(withReference = false)
            )

        val result = classUnderTest.membersWithConnectedPayinMethodForMarket(
            listOf("123", "234"),
            Market.NORWAY
        )
        assertThat(result.size).isEqualTo(1)
        assertThat(result[0]).isEqualTo("123")
    }

    @Test
    fun `if market is Denmark and one member has Adyen connected and one member does not have Adyen connected only return member with Adyen connected`() {

        every { memberAdyenAccountRepository.findAllByMemberIdIn(listOf("123", "234")) } returns
            listOf(
                buildAdyenAccount(),
                buildAdyenAccount(withReference = false)
            )

        val result = classUnderTest.membersWithConnectedPayinMethodForMarket(
            listOf("123", "234"),
            Market.DENMARK
        )

        assertThat(result.size).isEqualTo(1)
        assertThat(result[0]).isEqualTo("123")
    }

    @Test
    fun `if adyen accounts are null and market is Norway return empty list`() {
        every { memberAdyenAccountRepository.findAllByMemberIdIn(listOf()) } returns emptyList()

        val result = classUnderTest.membersWithConnectedPayinMethodForMarket(
            listOf(), Market.NORWAY
        )

        assertThat(result).isEmpty()
    }

    private fun buildDirectDebitAccountOrder(
        id: String = "321",
        trustlyAccountNumber: String = "5677",
        directDebitStatus: DirectDebitStatus? = null,
        createdAt: Instant = Instant.now()
    ) = DirectDebitAccountOrder(
        hedvigOrderId = UUID.randomUUID(),
        memberId = id,
        trustlyAccountId = trustlyAccountNumber,
        bank = "bank",
        descriptor = "**1234",
        directDebitStatus = directDebitStatus,
        createdAt = createdAt
    )

    private fun buildAdyenAccount(withReference: Boolean = true): MemberAdyenAccount {
        val account = MemberAdyenAccount("123", "account")
        account.recurringDetailReference = if (withReference) "reference" else null
        account.accountStatus = if (withReference) AdyenAccountStatus.AUTHORISED else null
        return account
    }
}
