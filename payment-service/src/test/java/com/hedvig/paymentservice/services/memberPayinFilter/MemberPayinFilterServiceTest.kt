package com.hedvig.paymentservice.services.memberPayinFilter

import com.hedvig.paymentservice.domain.payments.DirectDebitStatus
import com.hedvig.paymentservice.query.member.entities.Member
import com.hedvig.paymentservice.query.member.entities.MemberRepository
import com.hedvig.paymentservice.serviceIntergration.productPricing.ProductPricingService
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.ContractMarketInfo
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.Market
import com.hedvig.paymentservice.services.payinMethodFilter.MemberPayinMethodFilterService
import com.hedvig.paymentservice.services.payinMethodFilter.MemberPayinMethodFilterServiceImpl
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import javax.money.Monetary

@RunWith(MockitoJUnitRunner::class)
class MemberPayinFilterServiceTest {

    @Mock
    lateinit var memberRepository: MemberRepository

    @Mock
    lateinit var productPricingService: ProductPricingService

    lateinit var classUnderTest: MemberPayinMethodFilterService

    @Before
    fun setup() {
        classUnderTest = MemberPayinMethodFilterServiceImpl(
            memberRepository,
            productPricingService
        )
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


        whenever(memberRepository.findAll()).thenReturn(
            listOf(
                onlyHaveAccountNumber,
                onlyHaveDirectDebitStatus,
                accountNumberAndDirectDebitStatusConnected
            )
        )

        whenever(productPricingService.getContractMarketInfo(any())).thenReturn(
            ContractMarketInfo(Market.SWEDEN, Monetary.getCurrency("SEK"))
        )

        val result = classUnderTest.membersWithConnectedPayinMethodForMarket(Market.SWEDEN)

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

        whenever(memberRepository.findAll()).thenReturn(listOf(disconnectedDirectDebit, connectedDirectDebit))

        whenever(productPricingService.getContractMarketInfo(any())).thenReturn(
            ContractMarketInfo(Market.SWEDEN, Monetary.getCurrency("SEK"))
        )

        val result = classUnderTest.membersWithConnectedPayinMethodForMarket(Market.SWEDEN)

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

        whenever(productPricingService.getContractMarketInfo(any())).thenReturn(
            ContractMarketInfo(Market.NORWAY, Monetary.getCurrency("NOK"))
        )

        whenever(memberRepository.findAll()).thenReturn(listOf(withAdyenConnected, withAdyenPending))

        val result = classUnderTest.membersWithConnectedPayinMethodForMarket(Market.NORWAY)

        assertThat(result.size).isEqualTo(1)
        assertThat(result[0]).isEqualTo("123")
    }

    @Test
    fun `if market is Sweden will only return members with market of Sweden`() {

        val withAdyenConnected = buildMemberEntity(
            id = "123",
            adyenRecurringDetailReference = "5463",
            directDebitStatus = DirectDebitStatus.CONNECTED
        )
        val withTrustlyConnected = buildMemberEntity(
            id = "234",
            directDebitStatus = DirectDebitStatus.CONNECTED,
            trustlyAccountNumber = "2334"
        )

        whenever(memberRepository.findAll()).thenReturn(listOf(withAdyenConnected, withTrustlyConnected))

        whenever(productPricingService.getContractMarketInfo("123")).thenReturn(
            ContractMarketInfo(Market.NORWAY, Monetary.getCurrency("NOK"))
        )

        whenever(productPricingService.getContractMarketInfo("234")).thenReturn(
            ContractMarketInfo(Market.SWEDEN, Monetary.getCurrency("SEK"))
        )

        val result = classUnderTest.membersWithConnectedPayinMethodForMarket(Market.SWEDEN)

        assertThat(result.size).isEqualTo(1)
        assertThat(result[0]).isEqualTo("234")
    }

    @Test
    fun `if market is Denmark will only return members with adyen connected and market of Denmark`() {

        val danishMemberWithAdyenConnected = buildMemberEntity(
            id = "123",
            adyenRecurringDetailReference = "5463",
            directDebitStatus = DirectDebitStatus.CONNECTED
        )
        val norwegianMemberWithAdyenConnected = buildMemberEntity(
            id = "234",
            adyenRecurringDetailReference = "5463",
            directDebitStatus = DirectDebitStatus.CONNECTED
        )
        val swedishMemberWithTrustlyConnected = buildMemberEntity(
            id = "345",
            directDebitStatus = DirectDebitStatus.CONNECTED,
            trustlyAccountNumber = "2334"
        )

        whenever(memberRepository.findAll()).thenReturn(listOf(
            danishMemberWithAdyenConnected,
            norwegianMemberWithAdyenConnected,
            swedishMemberWithTrustlyConnected)
        )

        whenever(productPricingService.getContractMarketInfo("123")).thenReturn(
            ContractMarketInfo(Market.DENMARK, Monetary.getCurrency("DKK"))
        )

        whenever(productPricingService.getContractMarketInfo("234")).thenReturn(
            ContractMarketInfo(Market.NORWAY, Monetary.getCurrency("NOK"))
        )

        whenever(productPricingService.getContractMarketInfo("345")).thenReturn(
            ContractMarketInfo(Market.SWEDEN, Monetary.getCurrency("SEK"))
        )

        val result = classUnderTest.membersWithConnectedPayinMethodForMarket(Market.DENMARK)

        assertThat(result.size).isEqualTo(1)
        assertThat(result[0]).isEqualTo("123")
    }

    @Test
    fun `when market is Denmark return empty list if all members are Swedish or Norwegian`() {
        val connectedDirectDebit = buildMemberEntity(
            id = "123",
            trustlyAccountNumber = "111",
            directDebitStatus = DirectDebitStatus.CONNECTED
        )
        val adyenConnected = buildMemberEntity(
            id = "234",
            adyenRecurringDetailReference = "222",
            directDebitStatus = DirectDebitStatus.CONNECTED
        )

        whenever(memberRepository.findAll()).thenReturn(listOf(connectedDirectDebit, adyenConnected))

        whenever(productPricingService.getContractMarketInfo("123")).thenReturn(
            ContractMarketInfo(Market.SWEDEN, Monetary.getCurrency("SEK"))
        )

        whenever(productPricingService.getContractMarketInfo("234")).thenReturn(
            ContractMarketInfo(Market.NORWAY, Monetary.getCurrency("NOK"))
        )

        val result = classUnderTest.membersWithConnectedPayinMethodForMarket(Market.DENMARK)

        assertThat(result).isEmpty()
    }

    @Test
    fun `if members are null return empty list`() {
        whenever(memberRepository.findAll()).thenReturn(emptyList())

        val result = classUnderTest.membersWithConnectedPayinMethodForMarket(Market.NORWAY)

        assertThat(result).isEmpty()
    }

    @Test
    fun `if market is null for member return empty list`() {
        val connectedDirectDebit = buildMemberEntity(
            id = "123",
            trustlyAccountNumber = "111",
            directDebitStatus = DirectDebitStatus.CONNECTED
        )

        whenever(memberRepository.findAll()).thenReturn(listOf(connectedDirectDebit))

        whenever(productPricingService.getContractMarketInfo("234")).thenThrow(NullPointerException::class.java)

        val result = classUnderTest.membersWithConnectedPayinMethodForMarket(Market.SWEDEN)

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
