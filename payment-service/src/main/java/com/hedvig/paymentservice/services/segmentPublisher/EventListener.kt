package com.hedvig.paymentservice.services.segmentPublisher

import com.google.common.collect.ImmutableMap
import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountCreatedEvent
import com.segment.analytics.Analytics
import com.segment.analytics.messages.IdentifyMessage
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("SegmentProcessorGroup")
open class EventListener(private val segmentAnalytics: Analytics) {

  private val integrationSettings = mapOf("All" to false, "Customer.io" to true)

    @EventHandler
    fun on(evt: TrustlyAccountCreatedEvent) {


        val traits = ImmutableMap.of<String, Any>("is_direct_debit_activated", evt.isDirectDebitMandateActivated)

        segmentAnalytics.identify(traits, evt.memberId, integrationSettings)

    }

}
