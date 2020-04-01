package com.hedvig.paymentservice.services.segmentPublisher

import com.hedvig.paymentservice.domain.payments.events.DirectDebitConnectedEvent
import com.hedvig.paymentservice.domain.payments.events.DirectDebitDisconnectedEvent
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
}
