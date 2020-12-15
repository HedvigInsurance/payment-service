package com.hedvig.paymentservice.services.memberPayinFilter

import com.hedvig.paymentservice.domain.payments.DirectDebitStatus
import com.hedvig.paymentservice.query.member.entities.Member
import com.hedvig.paymentservice.query.member.entities.MemberRepository
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.Market
import com.hedvig.paymentservice.services.payinMethodFilter.MemberPayinMethodFilterService
import com.hedvig.paymentservice.services.payinMethodFilter.MemberPayinMethodFilterServiceImpl
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class MemberPayinFilterServiceTest {

    @Mock
    lateinit var memberRepository: MemberRepository

    private lateinit var classUnderTest: MemberPayinMethodFilterService

    @Before
    fun setup() {
        classUnderTest = MemberPayinMethodFilterServiceImpl(memberRepository)
    }

    @Test
    fun `if market is Sweden only return member if both trustly account number is present and direct debit status is connected`() {
        val onlyHaveAccountNumber = buildMemberEntity(
            id = "123",
            trustlyAccountNumber = "222",
            directDebitStatus = null
        )

        val onlyHaveDirectDebitStatus = buildMemberEntity(
            id = "234",
            trustlyAccountNumber = null,
            directDebitStatus = DirectDebitStatus.CONNECTED
        )

        val accountNumberAndDirectDebitStatusConnected = buildMemberEntity(
            id = "345",
            trustlyAccountNumber = "111",
            directDebitStatus = DirectDebitStatus.CONNECTED
        )


        whenever(memberRepository.findAllByIdIn( listOf("123", "234", "345"))).thenReturn(
            listOf(
                onlyHaveAccountNumber,
                onlyHaveDirectDebitStatus,
                accountNumberAndDirectDebitStatusConnected
            )
        )

        val result = classUnderTest.membersWithConnectedPayinMethodForMarket(listOf(
            "123", "234", "345"),
            Market.SWEDEN
        )

        assertThat(result.size).isEqualTo(1)
        assertThat(result[0]).isEqualTo("345")
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

        whenever(memberRepository.findAllByIdIn( listOf("123", "234"))).thenReturn(listOf(disconnectedDirectDebit, connectedDirectDebit))

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

        whenever(memberRepository.findAllByIdIn( listOf("123", "234"))).thenReturn(listOf(withAdyenConnected, withAdyenPending))

        val result = classUnderTest.membersWithConnectedPayinMethodForMarket(
            listOf("123", "234"),
            Market.NORWAY)

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

        whenever(memberRepository.findAllByIdIn( listOf("123", "234"))).thenReturn(listOf(withAdyenConnected, withAdyenPending))

        val result = classUnderTest.membersWithConnectedPayinMethodForMarket(
            listOf("123", "234"),
            Market.DENMARK)

        assertThat(result.size).isEqualTo(1)
        assertThat(result[0]).isEqualTo("123")
    }

    @Test
    fun `if members are null return empty list`() {
        whenever(memberRepository.findAllByIdIn(listOf())).thenReturn(emptyList())

        val result = classUnderTest.membersWithConnectedPayinMethodForMarket(
            listOf(), Market.NORWAY)

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
}
