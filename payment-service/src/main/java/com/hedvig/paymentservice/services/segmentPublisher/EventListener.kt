package com.hedvig.paymentservice.services.segmentPublisher

import com.hedvig.paymentservice.domain.payments.enums.AdyenAccountStatus
import com.hedvig.paymentservice.domain.payments.events.AdyenAccountCreatedEvent
import com.hedvig.paymentservice.domain.payments.events.AdyenAccountUpdatedEvent
import com.hedvig.paymentservice.domain.payments.events.DirectDebitConnectedEvent
import com.hedvig.paymentservice.domain.payments.events.DirectDebitDisconnectedEvent
import com.hedvig.paymentservice.graphQl.types.PayinMethodStatus
import com.hedvig.paymentservice.serviceIntergration.notificationService.NotificationService
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("customer.io")
@ProcessingGroup("SegmentProcessorGroup")
class EventListener(
    private val notificationService: NotificationService
) {
    @EventHandler
    fun on(evt: DirectDebitConnectedEvent) {
        val traits = mapOf("is_direct_debit_activated" to true)

        notificationService.updateCustomer(evt.memberId, traits)
    }

    @EventHandler
    fun on(evt: DirectDebitDisconnectedEvent) {
        val traits = mapOf("is_direct_debit_activated" to false)

        notificationService.updateCustomer(evt.memberId, traits)
    }

    @EventHandler
    fun on(e: AdyenAccountCreatedEvent) {
        updateTraitsBasedOnAdyenAccountStatus(e.memberId, e.accountStatus)
    }

    @EventHandler
    fun on(e: AdyenAccountUpdatedEvent) {
        updateTraitsBasedOnAdyenAccountStatus(e.memberId, e.accountStatus)
    }

    private fun updateTraitsBasedOnAdyenAccountStatus(memberId: String, adyenAccountStatus: AdyenAccountStatus) {
        val traits = when (PayinMethodStatus.fromAdyenAccountStatus(adyenAccountStatus)) {
            PayinMethodStatus.ACTIVE -> mapOf(IS_CARD_CONNECTED to true)
            PayinMethodStatus.PENDING, PayinMethodStatus.NEEDS_SETUP -> mapOf(IS_CARD_CONNECTED to false)
        }
        notificationService.updateCustomer(memberId, traits)
    }

    companion object {
        const val IS_CARD_CONNECTED: String = "is_card_connected"
    }
}
