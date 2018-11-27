package com.hedvig.paymentservice.services.segmentPublisher

import org.assertj.core.api.Assertions.assertThat
import org.mockito.BDDMockito.then

import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountCreatedEvent
import com.segment.analytics.Analytics
import com.segment.analytics.messages.IdentifyMessage
import com.segment.analytics.messages.MessageBuilder
import java.util.UUID
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
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
    fun trustlyAccountCreatedEvent_withDirectDebitMandateActivated_setsIsDirectDebitActivatedToTrue() {
        val evt = makeTrustlyAccountCreatedEvent(memberId = "1337", directDebitMandateActivated = true)

        val sut = EventListener(segmentAnalyticsMock)
        sut.on(evt)
        then<Analytics>(segmentAnalyticsMock).should().enqueue(enqueueCaptor.capture())

        val builtMessage = enqueueCaptor.value.build() as IdentifyMessage

        assertThat(builtMessage.userId()).isEqualTo("1337")
        assertThat(builtMessage.traits()).containsEntry("is_direct_debit_activated", true as Any)
    }


    @Test
    fun trustlyAccountCreatedEvent_withDirectDebitMandateNotActivated_setsIsDirectDebitActivatedToFalse() {
        val evt = makeTrustlyAccountCreatedEvent(directDebitMandateActivated = false)

        val sut = EventListener(segmentAnalyticsMock)
        sut.on(evt)
        then<Analytics>(segmentAnalyticsMock).should().enqueue(enqueueCaptor.capture())

        val builtMessage = enqueueCaptor.value.build() as IdentifyMessage

        assertThat(builtMessage.traits()).containsEntry("is_direct_debit_activated", false as Any)
    }

    private fun makeTrustlyAccountCreatedEvent(
        memberId: String = "1337",
        directDebitMandateActivated: Boolean,
        productId: UUID = UUID.fromString("8c8fc0ea-f27e-11e8-861b-87f3ef28e42a"),
        trustlyAccountId: String = "a57b64ce-f27e-11e8-b740-bb89cb8cfdb9",
        address: String = "Some address"): TrustlyAccountCreatedEvent {
        return TrustlyAccountCreatedEvent(
            memberId,
            productId,
            trustlyAccountId,
            address, "SWEDBANK", "Stockholm", "SWE", "descriptor", directDebitMandateActivated, "XXXX", "Tolvan", "19121212-1212", "12345")
    }
}
