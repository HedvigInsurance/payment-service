package com.hedvig.paymentservice.services.segmentPublisher

import com.hedvig.paymentservice.domain.payments.DirectDebitStatus
import com.hedvig.paymentservice.domain.payments.events.DirectDebitConnectedEvent
import com.hedvig.paymentservice.domain.payments.events.DirectDebitDisconnectedEvent
import com.hedvig.paymentservice.query.member.entities.Member
import com.hedvig.paymentservice.query.member.entities.MemberRepository
import com.hedvig.paymentservice.serviceIntergration.notificationService.NotificationService
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
class EventListener(
    val memberRepository: MemberRepository,
    val notificationService: NotificationService
) {
    @EventHandler
    fun on(evt: DirectDebitConnectedEvent) {
        updateNotificationService(DirectDebitStatus.CONNECTED, evt.memberId, evt.trustlyAccountId)
    }

    @EventHandler
    fun on(evt: DirectDebitDisconnectedEvent) {
        updateNotificationService(DirectDebitStatus.DISCONNECTED, evt.memberId, evt.trustlyAccountId)
    }

    private fun updateNotificationService(status: DirectDebitStatus, memberId: String, trustlyAccountId: String) {
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

    private fun isUpdateForTheLatestTrustlyAccount(member: Member, trustlyAccountInQuestion: String): Boolean {
        return member.trustlyAccountNumber == null || member.trustlyAccountNumber == trustlyAccountInQuestion
    }

    companion object {
        const val IS_DIRECT_DEBIT_ACTIVATED: String = "is_direct_debit_activated"
        val log = LoggerFactory.getLogger(this::class.java)!!
    }
}
