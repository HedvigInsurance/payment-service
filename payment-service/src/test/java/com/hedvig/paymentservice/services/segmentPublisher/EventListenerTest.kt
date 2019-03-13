package com.hedvig.paymentservice.services.segmentPublisher

import com.hedvig.paymentservice.domain.payments.events.DirectDebitConnectedEvent
import com.hedvig.paymentservice.domain.payments.events.DirectDebitDisconnectedEvent
import com.hedvig.paymentservice.domain.payments.events.DirectDebitPendingConnectionEvent
import com.segment.analytics.Analytics
import com.segment.analytics.messages.IdentifyMessage
import com.segment.analytics.messages.MessageBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.BDDMockito.then
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class EventListenerTest {

  @Mock
  internal lateinit var segmentAnalyticsMock: Analytics

  @Captor
  lateinit var enqueueCaptor: ArgumentCaptor<MessageBuilder<*, *>>

  @Test
  fun DirectDebitConnectedEvent_withDirectDebitMandateActivated_setsIsDirectDebitActivatedToTrue() {
    val evt = DirectDebitConnectedEvent(
      MEMBER_ID,
      HEDVIG_ORDER_ID,
      TRUSTLY_ACCOUNT_ID
    )

    val sut = EventListener(segmentAnalyticsMock)
    sut.on(evt)
    then<Analytics>(segmentAnalyticsMock).should().enqueue(enqueueCaptor.capture())

    val builtMessage = enqueueCaptor.value.build() as IdentifyMessage

    assertThat(builtMessage.userId()).isEqualTo(MEMBER_ID)
    assertThat(builtMessage.traits()).containsEntry("is_direct_debit_activated", true as Any)
  }


  @Test
  fun DirectDebitDisConnectedEvent_withDirectDebitMandateNotActivated_setsIsDirectDebitActivatedToFalse() {
    val evt = DirectDebitDisconnectedEvent(
      MEMBER_ID,
      HEDVIG_ORDER_ID,
      TRUSTLY_ACCOUNT_ID
    )

    val sut = EventListener(segmentAnalyticsMock)
    sut.on(evt)
    then<Analytics>(segmentAnalyticsMock).should().enqueue(enqueueCaptor.capture())

    val builtMessage = enqueueCaptor.value.build() as IdentifyMessage

    assertThat(builtMessage.userId()).isEqualTo(MEMBER_ID)
    assertThat(builtMessage.traits()).containsEntry("is_direct_debit_activated", false as Any)
  }

  companion object {
    const val MEMBER_ID: String = "1234"
    const val HEDVIG_ORDER_ID: String = "ME GUSTA HEDVIG"
    const val TRUSTLY_ACCOUNT_ID: String = "ME LIKE TRUSTLY"
  }

}
