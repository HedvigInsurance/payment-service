package com.hedvig.paymentservice.services.segmentPublisher


import com.hedvig.paymentservice.domain.payments.TransactionType
import com.hedvig.paymentservice.domain.payments.events.ChargeFailedEvent
import com.hedvig.paymentservice.query.member.entities.Member
import com.hedvig.paymentservice.query.member.entities.MemberRepository
import com.hedvig.paymentservice.query.member.entities.Transaction
import com.segment.analytics.Analytics
import com.segment.analytics.messages.MessageBuilder
import com.segment.analytics.messages.TrackMessage
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class LiveEventListenerTest {

  private val PRODUCT_ID = "0ebc7d7a-f34b-11e8-ac63-3be345834e94"

  @Mock
  lateinit var segementAnalytics: Analytics

  @Mock
  lateinit var memberRepository: MemberRepository

  @Captor
  lateinit var enqueueCaptor: ArgumentCaptor<MessageBuilder<*, *>>

  @Test
  fun chargeFailedEvent_disablesDefaultIntegrationAndEnablesCustomerIO() {
    val memberEntity = makeDefaultMember("1337")

    memberEntity.makeChargeTransaction(PRODUCT_ID)
    given(memberRepository.findById("1337")).willReturn(Optional.of(memberEntity))

    val evt = ChargeFailedEvent("1337", UUID.fromString(PRODUCT_ID))


    val sut = LiveEventListener(segementAnalytics, memberRepository)

    sut.on(evt)

    then(segementAnalytics).should().enqueue(enqueueCaptor.capture())
    val builtMessage = enqueueCaptor.value.build() as TrackMessage

    assertThat(builtMessage.event()).isEqualTo("Charge Failed")
    assertThat(builtMessage.userId()).isEqualTo("1337")
    assertThat(builtMessage.integrations()).containsEntry("All", false)
    assertThat(builtMessage.integrations()).containsEntry("Customer.io", true)

  }

  @Test
  fun chargeFailedEvent_givenTransaction_setsAmountAndCurrency() {
    val memberEntity = makeDefaultMember("1337")
    memberEntity.makeChargeTransaction(PRODUCT_ID, amount = BigDecimal.TEN)
    given(memberRepository.findById("1337")).willReturn(Optional.of(memberEntity))

    val evt = ChargeFailedEvent("1337", UUID.fromString(PRODUCT_ID))

    val sut = LiveEventListener(segementAnalytics, memberRepository)

    sut.on(evt)

    then(segementAnalytics).should().enqueue(enqueueCaptor.capture())
    val builtMessage = enqueueCaptor.value.build() as TrackMessage

    assertThat(builtMessage.userId()).isEqualTo("1337")
    assertThat(builtMessage.event()).isEqualTo("Charge Failed")
    assertThat(builtMessage.properties()).containsEntry("amount", "10,00")
    assertThat(builtMessage.properties()).containsEntry("currency", "SEK")

  }

  @Test
  fun chargeFailedEvent_givenTransaction_setsEventNameAndMemberId() {
    val memberEntity = makeDefaultMember("1337")
    memberEntity.makeChargeTransaction(PRODUCT_ID)
    given(memberRepository.findById("1337")).willReturn(Optional.of(memberEntity))

    val evt = ChargeFailedEvent("1337", UUID.fromString(PRODUCT_ID))

    val sut = LiveEventListener(segementAnalytics, memberRepository)

    sut.on(evt)

    then(segementAnalytics).should().enqueue(enqueueCaptor.capture())
    val builtMessage = enqueueCaptor.value.build() as TrackMessage

    assertThat(builtMessage.userId()).isEqualTo("1337")
    assertThat(builtMessage.event()).isEqualTo("Charge Failed")

  }

  @Test
  fun chargeFailedEvent_givenTransactionWithManyDecimals_setsAmountWithTwoDecimals() {
    val memberEntity = makeDefaultMember("1337")
    memberEntity.makeChargeTransaction(PRODUCT_ID, amount = BigDecimal.valueOf(10.00))
    given(memberRepository.findById("1337")).willReturn(Optional.of(memberEntity))

    val evt = ChargeFailedEvent("1337", UUID.fromString(PRODUCT_ID))

    val sut = LiveEventListener(segementAnalytics, memberRepository)

    sut.on(evt)

    then(segementAnalytics).should().enqueue(enqueueCaptor.capture())
    val builtMessage = enqueueCaptor.value.build() as TrackMessage

    assertThat(builtMessage.properties()).containsEntry("amount", "10,00")
    assertThat(builtMessage.properties()).containsEntry("currency", "SEK")

  }

  private fun Member.makeChargeTransaction(transactionId:String, amount:BigDecimal = BigDecimal.TEN){
    val transaction = Transaction()
    transaction.id = UUID.fromString(transactionId)
    transaction.setAmount(amount)
    transaction.transactionType = TransactionType.CHARGE
    transaction.setCurrency("SEK")
    transaction.timestamp = Instant.parse("2018-09-11T23:00:00Z")
    this.transactions[transaction.id] = transaction
  }

  private fun makeDefaultMember(memberId:String = "1337"): Member {
    val member = Member()
    member.id = memberId
    member.isDirectDebitMandateActive = true
    member.trustlyAccountNumber = "32094820834"

    return member
  }
}
