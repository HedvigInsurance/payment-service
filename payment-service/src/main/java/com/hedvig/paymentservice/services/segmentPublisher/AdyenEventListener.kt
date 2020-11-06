package com.hedvig.paymentservice.services.segmentPublisher

import com.hedvig.paymentservice.domain.payments.enums.AdyenAccountStatus
import com.hedvig.paymentservice.domain.payments.events.AdyenAccountCreatedEvent
import com.hedvig.paymentservice.domain.payments.events.AdyenAccountUpdatedEvent
import com.hedvig.paymentservice.graphQl.types.PayinMethodStatus
import com.hedvig.paymentservice.serviceIntergration.notificationService.NotificationService
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Profile("customer.io")
@ProcessingGroup("AdyenSegmentProcessorGroup")
class AdyenEventListener(
    val notificationService: NotificationService
) {

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
