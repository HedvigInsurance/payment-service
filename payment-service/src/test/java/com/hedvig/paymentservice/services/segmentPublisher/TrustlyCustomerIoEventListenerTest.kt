package com.hedvig.paymentservice.services.segmentPublisher

import com.hedvig.paymentservice.domain.payments.events.DirectDebitConnectedEvent
import com.hedvig.paymentservice.domain.payments.events.DirectDebitDisconnectedEvent
import com.hedvig.paymentservice.serviceIntergration.notificationService.NotificationService
import com.nhaarman.mockitokotlin2.KArgumentCaptor
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class TrustlyCustomerIoEventListenerTest {

    @Mock
    internal lateinit var notificationService: NotificationService

    lateinit var sut: TrustlyCustomerIoEventListener

    lateinit var dataCaptor: KArgumentCaptor<Map<String, Any>>

    @Before
    fun setup() {
        dataCaptor = argumentCaptor()
        sut = TrustlyCustomerIoEventListener(notificationService)
    }

    @Test
    fun DirectDebitConnectedEvent_withDirectDebitMandateActivated_setsIsDirectDebitActivatedToTrue() {
        val evt = DirectDebitConnectedEvent(
            MEMBER_ID,
            HEDVIG_ORDER_ID,
            TRUSTLY_ACCOUNT_ID
        )
        sut.on(evt)
        then(notificationService).should().updateCustomer(eq(MEMBER_ID), dataCaptor.capture())

        assertThat(dataCaptor.firstValue).containsEntry("is_direct_debit_activated", true as Object)
    }

    @Test
    fun DirectDebitDisConnectedEvent_withDirectDebitMandateNotActivated_setsIsDirectDebitActivatedToFalse() {
        val evt = DirectDebitDisconnectedEvent(
            MEMBER_ID,
            HEDVIG_ORDER_ID,
            TRUSTLY_ACCOUNT_ID
        )
        sut.on(evt)
        then(notificationService).should().updateCustomer(eq(MEMBER_ID), dataCaptor.capture())

        assertThat(dataCaptor.firstValue).containsEntry("is_direct_debit_activated", false as Object)
    }

    companion object {
        const val MEMBER_ID: String = "1234"
        const val HEDVIG_ORDER_ID: String = "ME GUSTA HEDVIG"
        const val TRUSTLY_ACCOUNT_ID: String = "ME LIKE TRUSTLY"
    }
}
