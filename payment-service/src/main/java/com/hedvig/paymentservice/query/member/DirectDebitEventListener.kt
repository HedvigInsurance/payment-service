package com.hedvig.paymentservice.query.member

import com.hedvig.paymentservice.domain.payments.DirectDebitStatus
import com.hedvig.paymentservice.domain.payments.events.DirectDebitConnectedEvent
import com.hedvig.paymentservice.domain.payments.events.DirectDebitDisconnectedEvent
import com.hedvig.paymentservice.domain.payments.events.DirectDebitPendingConnectionEvent
import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountCreatedEvent
import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountUpdatedEvent
import com.hedvig.paymentservice.query.member.entities.MemberRepository
import lombok.extern.slf4j.Slf4j
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Slf4j
@Order(2)
@ProcessingGroup("DirectDebitProcessorGroup")
class DirectDebitEventListener(
    val memberRepository: MemberRepository
) {
    @EventHandler
    fun on(e: TrustlyAccountCreatedEvent) {
        val member = memberRepository.findById(e.memberId)
        if (!member.isPresent) {
            logger.error("Could not find member")
            return
        }
        val m = member.get()
        m.trustlyAccountNumber = e.trustlyAccountId
        m.bank = e.bank
        m.descriptor = e.descriptor
        memberRepository.save(m)
    }

    @EventHandler
    fun on(e: TrustlyAccountUpdatedEvent) {
        val member = memberRepository.findById(e.memberId)
        if (!member.isPresent) {
            logger.error("Could not find member")
            return
        }
        val m = member.get()
        m.trustlyAccountNumber = e.trustlyAccountId
        m.bank = e.bank
        m.descriptor = e.descriptor
        memberRepository.save(m)
    }

    @EventHandler
    fun on(e: DirectDebitConnectedEvent) {
        updateDirectDebitStatus(DirectDebitStatus.CONNECTED, e.memberId, e.trustlyAccountId)
    }

    @EventHandler
    fun on(e: DirectDebitPendingConnectionEvent) {
        updateDirectDebitStatus(DirectDebitStatus.PENDING, e.memberId, e.trustlyAccountId)
    }

    @EventHandler
    fun on(e: DirectDebitDisconnectedEvent) {
        updateDirectDebitStatus(DirectDebitStatus.DISCONNECTED, e.memberId, e.trustlyAccountId)
    }

    private fun updateDirectDebitStatus(status: DirectDebitStatus, memberId: String, trustlyAccountId: String) {
        val optionalMember = memberRepository.findById(memberId)
        if (!optionalMember.isPresent) {
            logger.error(
                "Cannot update direct debit status! Member {} cannot be found. TrustlyAccountId: {}",
                memberId,
                trustlyAccountId
            )
            return
        }
        val m = optionalMember.get()
        m.directDebitStatus = status
        memberRepository.save(m)
    }

    companion object{
        val logger = LoggerFactory.getLogger(this::class.java)!!
    }
}
