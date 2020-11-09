package com.hedvig.paymentservice.services.segmentPublisher

import com.hedvig.paymentservice.domain.payments.DirectDebitStatus
import com.hedvig.paymentservice.domain.payments.events.DirectDebitConnectedEvent
import com.hedvig.paymentservice.domain.payments.events.DirectDebitDisconnectedEvent
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
@ProcessingGroup("TrustlySegmentProcessorGroup")
@Order(1)
class TrustlyCustomerIoEventListener(
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

    companion object {
        const val IS_DIRECT_DEBIT_ACTIVATED: String = "is_direct_debit_activated"
        val log = LoggerFactory.getLogger(this::class.java)!!
    }
}
