package com.hedvig.paymentservice.services.memberPayinFilter

import com.hedvig.paymentservice.PaymentServiceTestConfiguration
import com.hedvig.paymentservice.domain.payments.DirectDebitStatus
import com.hedvig.paymentservice.query.directDebit.DirectDebitAccountOrder
import com.hedvig.paymentservice.query.directDebit.DirectDebitAccountOrderRepository
import com.hedvig.paymentservice.query.member.entities.Member
import com.hedvig.paymentservice.query.member.entities.MemberRepository
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.Market
import com.hedvig.paymentservice.services.payinMethodFilter.MemberPayinMethodFilterService
import com.hedvig.paymentservice.services.payinMethodFilter.MemberPayinMethodFilterServiceImpl
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@RunWith(SpringRunner::class)
@DataJpaTest
@ContextConfiguration(classes = [PaymentServiceTestConfiguration::class])
class MemberPayinFilterServiceTest {
    @MockkBean
    lateinit var memberRepository: MemberRepository

    @Autowired
    lateinit var directDebitAccountOrderRepository: DirectDebitAccountOrderRepository

    private lateinit var classUnderTest: MemberPayinMethodFilterService

    @Before
    fun setup() {
        classUnderTest = MemberPayinMethodFilterServiceImpl(memberRepository, directDebitAccountOrderRepository)
    }

    @Test
    fun `if market is Sweden only return member if both trustly account number is present and direct debit status is connected`() {
        val onlyHaveAccountNumber = buildDirectDebitAccountOrder(
            id = "123",
            trustlyAccountNumber = "222",
            directDebitStatus = DirectDebitStatus.CONNECTED
        )

        val onlyHaveDirectDebitStatus = buildDirectDebitAccountOrder(
            id = "234",
            trustlyAccountNumber = "accountOne",
            directDebitStatus = DirectDebitStatus.CONNECTED,
            createdAt = Instant.now().minus(2, ChronoUnit.DAYS)
        )

        val accountNumberAndDirectDebitStatusConnected = buildDirectDebitAccountOrder(
            id = "234",
            trustlyAccountNumber = "accountTwo",
            directDebitStatus = DirectDebitStatus.DISCONNECTED,
            createdAt = Instant.now().minus(1, ChronoUnit.DAYS)
        )

        val accountNumberAndDirectDebitStatusConnectedTwo = buildDirectDebitAccountOrder(
            id = "234",
            trustlyAccountNumber = "accountTwo",
            directDebitStatus = DirectDebitStatus.CONNECTED,
            createdAt = Instant.now().minus(0, ChronoUnit.DAYS)
        )


        directDebitAccountOrderRepository.saveAll(listOf(
            onlyHaveAccountNumber,
            onlyHaveDirectDebitStatus,
            accountNumberAndDirectDebitStatusConnected,
            accountNumberAndDirectDebitStatusConnectedTwo
        ))

        val result = classUnderTest.membersWithConnectedPayinMethodForMarket(
            listOf(
                "123", "234", "345"
            ),
            Market.SWEDEN
        )

        assertThat(result.size).isEqualTo(2)
    }

    @Test
    fun `if market is Sweden and one member has DD connected and one member has DD disconnected only return member with DD connected`() {
        val disconnectedDirectDebit = buildMemberEntity(
            id = "123",
            trustlyAccountNumber = "111",
            directDebitStatus = DirectDebitStatus.DISCONNECTED
        )
        val connectedDirectDebit = buildMemberEntity(
            id = "234",
            trustlyAccountNumber = "222",
            directDebitStatus = DirectDebitStatus.CONNECTED
        )

        every { memberRepository.findAllByIdIn(listOf("123", "234")) } returns listOf(
            disconnectedDirectDebit,
            connectedDirectDebit
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
        val withAdyenConnected = buildMemberEntity(
            id = "123",
            adyenRecurringDetailReference = "5463",
            directDebitStatus = DirectDebitStatus.CONNECTED
        )
        val withAdyenPending = buildMemberEntity(
            id = "234",
            adyenRecurringDetailReference = null
        )

        every { memberRepository.findAllByIdIn(listOf("123", "234")) } returns listOf(
            withAdyenConnected,
            withAdyenPending
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
        val withAdyenConnected = buildMemberEntity(
            id = "123",
            adyenRecurringDetailReference = "5463"
        )
        val withAdyenPending = buildMemberEntity(
            id = "234",
            adyenRecurringDetailReference = null
        )

        every { memberRepository.findAllByIdIn(listOf("123", "234")) } returns listOf(
            withAdyenConnected,
            withAdyenPending
        )

        val result = classUnderTest.membersWithConnectedPayinMethodForMarket(
            listOf("123", "234"),
            Market.DENMARK
        )

        assertThat(result.size).isEqualTo(1)
        assertThat(result[0]).isEqualTo("123")
    }

    @Test
    fun `if members are null return empty list`() {
        every { memberRepository.findAllByIdIn(listOf()) } returns emptyList()

        val result = classUnderTest.membersWithConnectedPayinMethodForMarket(
            listOf(), Market.NORWAY
        )

        assertThat(result).isEmpty()
    }

    private fun buildMemberEntity(
        id: String = "321",
        trustlyAccountNumber: String? = null,
        adyenRecurringDetailReference: String? = null,
        directDebitStatus: DirectDebitStatus? = null
    ): Member {
        val member = Member()
        member.id = id
        member.trustlyAccountNumber = trustlyAccountNumber
        member.adyenRecurringDetailReference = adyenRecurringDetailReference
        member.directDebitStatus = directDebitStatus

        return member
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
}
