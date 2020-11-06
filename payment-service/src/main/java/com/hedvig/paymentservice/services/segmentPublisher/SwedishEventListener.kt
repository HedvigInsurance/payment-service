package com.hedvig.paymentservice.services.segmentPublisher

import com.hedvig.paymentservice.domain.payments.DirectDebitStatus
import com.hedvig.paymentservice.domain.payments.enums.AdyenAccountStatus
import com.hedvig.paymentservice.domain.payments.events.AdyenAccountCreatedEvent
import com.hedvig.paymentservice.domain.payments.events.AdyenAccountUpdatedEvent
import com.hedvig.paymentservice.domain.payments.events.DirectDebitConnectedEvent
import com.hedvig.paymentservice.domain.payments.events.DirectDebitDisconnectedEvent
import com.hedvig.paymentservice.graphQl.types.PayinMethodStatus
import com.hedvig.paymentservice.query.member.entities.MemberRepository
import com.hedvig.paymentservice.serviceIntergration.notificationService.NotificationService
import com.hedvig.paymentservice.util.isUpdateForTheLatestTrustlyAccount
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Profile("customer.io")
@ProcessingGroup("SegmentProcessorGroup")
@Order(1)
class SwedishEventListener(
    val memberRepository: MemberRepository,
    val notificationService: NotificationService
) {
    @EventHandler
    fun on(evt: DirectDebitConnectedEvent) {
        updateTraitsBasedOnDirectDebitStatus(DirectDebitStatus.CONNECTED, evt.memberId, evt.trustlyAccountId)
    }

    @EventHandler
    fun on(evt: DirectDebitDisconnectedEvent) {
        updateTraitsBasedOnDirectDebitStatus(DirectDebitStatus.DISCONNECTED, evt.memberId, evt.trustlyAccountId)
    }

    @EventHandler
    fun on(e: AdyenAccountCreatedEvent) {
        updateTraitsBasedOnAdyenAccountStatus(e.memberId, e.accountStatus)
    }

    @EventHandler
    fun on(e: AdyenAccountUpdatedEvent) {
        updateTraitsBasedOnAdyenAccountStatus(e.memberId, e.accountStatus)
    }

    private fun updateTraitsBasedOnDirectDebitStatus(status: DirectDebitStatus, memberId: String, trustlyAccountId: String) {
        val optionalMember = memberRepository.findById(memberId)
        if (!optionalMember.isPresent) {
            log.error(
                "Cannot update direct debit status in notification service " +
                    "Member $memberId cannot be found. TrustlyAccountId: $trustlyAccountId Status: ${status.name}"
            )
            return
        }
        val m = optionalMember.get()
        if (!isUpdateForTheLatestTrustlyAccount(m, trustlyAccountId)) {
            return
        }
        val traits = when (status) {
            DirectDebitStatus.PENDING,
            DirectDebitStatus.DISCONNECTED -> mapOf(IS_DIRECT_DEBIT_ACTIVATED to false)
            DirectDebitStatus.CONNECTED -> mapOf(IS_DIRECT_DEBIT_ACTIVATED to true)
        }
        notificationService.updateCustomer(memberId, traits)
    }

    private fun updateTraitsBasedOnAdyenAccountStatus(memberId: String, adyenAccountStatus: AdyenAccountStatus) {
        val traits = when (PayinMethodStatus.fromAdyenAccountStatus(adyenAccountStatus)) {
            PayinMethodStatus.ACTIVE -> mapOf(IS_CARD_CONNECTED to true)
            PayinMethodStatus.PENDING, PayinMethodStatus.NEEDS_SETUP -> mapOf(IS_CARD_CONNECTED to false)
        }
        notificationService.updateCustomer(memberId, traits)
    }

    companion object {
        const val IS_DIRECT_DEBIT_ACTIVATED: String = "is_direct_debit_activated"
        const val IS_CARD_CONNECTED: String = "is_card_connected"
        val log = LoggerFactory.getLogger(this::class.java)!!
    }
}
