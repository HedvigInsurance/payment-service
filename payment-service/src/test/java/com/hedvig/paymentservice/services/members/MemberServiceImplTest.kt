package com.hedvig.paymentservice.services.members

import com.hedvig.paymentservice.query.member.entities.MemberRepository
import com.hedvig.paymentservice.serviceIntergration.memberService.MemberServiceClient
import com.hedvig.paymentservice.serviceIntergration.memberService.MemberServiceImpl
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import com.hedvig.paymentservice.query.member.entities.Member
import com.hedvig.paymentservice.web.dtos.PaymentProvider
import org.assertj.core.api.Assertions.assertThat

@RunWith(MockitoJUnitRunner::class)
class MemberServiceImplTest {

  @Mock
  lateinit var memberServiceClient: MemberServiceClient
  @Mock
  lateinit var memberRepository: MemberRepository

  lateinit var classUnderTest: MemberServiceImpl

  @Before
  fun before() {
    classUnderTest = MemberServiceImpl(
      memberServiceClient,
      memberRepository
    )
  }

  @Test
  fun `get one member with trustly connected when one member with trustly is provided`() {
    whenever(memberRepository.findAll()).thenReturn(listOf(
      buildMemberEntity(
        trustlyAccountNumber = "123"
      )
    ))

    val result = classUnderTest.getMembersConnectedToProvider(PaymentProvider.TRUSTLY)

    assertThat(result).contains("321")
  }

  @Test
  fun `get two member with trustly connected when two member with trustly is provided`() {
    whenever(memberRepository.findAll()).thenReturn(listOf(
      buildMemberEntity(
        trustlyAccountNumber = "123"
      ),
      buildMemberEntity(
        id = "1234",
        trustlyAccountNumber = "123"
      )
    ))

    val result = classUnderTest.getMembersConnectedToProvider(PaymentProvider.TRUSTLY)

    assertThat(result).contains("321", "1234")
  }

  @Test
  fun `get one member with adyen connected when one member with adyen is provided`() {
    whenever(memberRepository.findAll()).thenReturn(listOf(
      buildMemberEntity(
        adyenRecurringDetailReference = "ref"
      )
    ))

    val result = classUnderTest.getMembersConnectedToProvider(PaymentProvider.ADYEN)

    assertThat(result).contains("321")
  }


  @Test
  fun `get one member with trustly connected when one member with trustly and one with adyen is provided`() {
    whenever(memberRepository.findAll()).thenReturn(listOf(
      buildMemberEntity(
        trustlyAccountNumber = "123"
      ),
      buildMemberEntity(
        id = "1234",
        adyenRecurringDetailReference = "ref"
      )
    ))

    val result = classUnderTest.getMembersConnectedToProvider(PaymentProvider.TRUSTLY)

    assertThat(result).contains("321")
  }

  @Test
  fun `get one member with adyen connected when one member with trustly and one with adyen is provided`() {
    whenever(memberRepository.findAll()).thenReturn(listOf(
      buildMemberEntity(
        trustlyAccountNumber = "123"
      ),
      buildMemberEntity(
        id = "1234",
        adyenRecurringDetailReference = "ref"
      )
    ))

    val result = classUnderTest.getMembersConnectedToProvider(PaymentProvider.ADYEN)

    assertThat(result).contains("1234")
  }

  @Test
  fun `get two members when fetching for all providers and when one member with trustly and one with adyen is provided`() {
    whenever(memberRepository.findAll()).thenReturn(listOf(
      buildMemberEntity(
        trustlyAccountNumber = "123"
      ),
      buildMemberEntity(
        id = "1234",
        adyenRecurringDetailReference = "ref"
      )
    ))

    val result = classUnderTest.getMembersConnectedToProvider(PaymentProvider.ALL)

    assertThat(result).contains("321", "1234")
  }

  private fun buildMemberEntity(
    id: String = "321",
    trustlyAccountNumber: String? = null,
    adyenRecurringDetailReference: String? = null
  ): Member {
    val member = Member()
    member.id = id
    member.trustlyAccountNumber = trustlyAccountNumber
    member.adyenRecurringDetailReference = adyenRecurringDetailReference

    return member
  }
}
