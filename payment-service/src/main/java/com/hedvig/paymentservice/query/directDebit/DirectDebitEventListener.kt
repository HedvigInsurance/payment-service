package com.hedvig.paymentservice.query.directDebit

import com.hedvig.paymentservice.domain.payments.DirectDebitStatus
import com.hedvig.paymentservice.domain.payments.events.DirectDebitConnectedEvent
import com.hedvig.paymentservice.domain.payments.events.DirectDebitDisconnectedEvent
import com.hedvig.paymentservice.domain.payments.events.DirectDebitPendingConnectionEvent
import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountCreatedEvent
import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountUpdatedEvent
import java.time.Instant
import java.util.*
import lombok.extern.slf4j.Slf4j
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.Timestamp
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Slf4j
@Order(1)
@ProcessingGroup("DirectDebitProcessorGroup")
class DirectDebitEventListener(
    val directDebitAccountOrderRepository: DirectDebitAccountOrderRepository
) {
    @EventHandler
    fun on(event: TrustlyAccountCreatedEvent, @Timestamp timestamp: Instant) {
        createAndSaveDirectDebitAccountOrder(event, timestamp)
    }

    private fun createAndSaveDirectDebitAccountOrder(
        event: TrustlyAccountCreatedEvent,
        timestamp: Instant
    ) {
        val directDebitAccountOrder = DirectDebitAccountOrder.fromTrustlyAccountCreatedEvent(event, timestamp)
        directDebitAccountOrderRepository.save(directDebitAccountOrder)
    }

    @EventHandler
    fun on(event: TrustlyAccountUpdatedEvent, @Timestamp timestamp: Instant) {
        val directDebitAccountOrders = directDebitAccountOrderRepository.findAllByMemberId(event.memberId)

        if (directDebitAccountOrders.none { it.hedvigOrderId == event.hedvigOrderId }) {
            val directDebitAccountOrder = DirectDebitAccountOrder.fromTrustlyAccountUpdatedEvent(event, timestamp)
            directDebitAccountOrderRepository.save(directDebitAccountOrder)
        }
    }

    @EventHandler
    fun on(event: DirectDebitConnectedEvent) {
        updateDirectDebitStatus(DirectDebitStatus.CONNECTED, event.hedvigOrderId)
    }

    @EventHandler
    fun on(event: DirectDebitPendingConnectionEvent) {
        updateDirectDebitStatus(DirectDebitStatus.PENDING, event.hedvigOrderId)
    }

    @EventHandler
    fun on(event: DirectDebitDisconnectedEvent) {
        updateDirectDebitStatus(DirectDebitStatus.DISCONNECTED, event.hedvigOrderId)
    }

    private fun updateDirectDebitStatus(status: DirectDebitStatus, hedvigOrderId: String) {
        val optionalDirectDebitAccountOrder = directDebitAccountOrderRepository.findById(UUID.fromString(hedvigOrderId))
        if (!optionalDirectDebitAccountOrder.isPresent) {
            logger.error(
                "Cannot update direct debit status! DirectDebitAccountOrder cannot be found for hedvigOrderId {}",
                hedvigOrderId
            )
            return
        }
        val directDebitAccountOrder = optionalDirectDebitAccountOrder.get()

        directDebitAccountOrder.apply { directDebitStatus = status }

        directDebitAccountOrderRepository.save(directDebitAccountOrder)
    }

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)!!
    }
}
