package com.hedvig.paymentservice.services.segmentPublisher

import com.google.common.collect.ImmutableMap
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
  fun on(evt: TrustlyAccountCreatedEvent) {
    val traits = ImmutableMap.of<String, Any>("is_direct_debit_activated", false) //TODO: FIX ME

    segmentAnalytics.identify(traits, evt.memberId, integrationSettings)
  }
}
