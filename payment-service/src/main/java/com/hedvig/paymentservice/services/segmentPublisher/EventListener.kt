package com.hedvig.paymentservice.services.segmentPublisher

import com.google.common.collect.ImmutableMap
import com.hedvig.paymentservice.domain.payments.events.DirectDebitConnectedEvent
import com.hedvig.paymentservice.domain.payments.events.DirectDebitDisconnectedEvent
import com.hedvig.paymentservice.domain.payments.events.DirectDebitPendingConnectionEvent
import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountCreatedEvent
import com.segment.analytics.Analytics
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("customer.io")
@ProcessingGroup("SegmentProcessorGroup")
class EventListener(private val segmentAnalytics: Analytics) {
  private val integrationSettings = mapOf("All" to false, "Customer.io" to true)

  @EventHandler
  fun on(evt: DirectDebitConnectedEvent) {
    val traits = ImmutableMap.of<String, Any>("is_direct_debit_activated", true)

    segmentAnalytics.identify(traits, evt.memberId, integrationSettings)
  }

  @EventHandler
  fun on(evt: DirectDebitPendingConnectionEvent) {
    val traits = ImmutableMap.of<String, Any>("is_direct_debit_activated", false)

    segmentAnalytics.identify(traits, evt.memberId, integrationSettings)
  }


  @EventHandler
  fun on(evt: DirectDebitDisconnectedEvent) {
    val traits = ImmutableMap.of<String, Any>("is_direct_debit_activated", false)

    segmentAnalytics.identify(traits, evt.memberId, integrationSettings)
  }
}
