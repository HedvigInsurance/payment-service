package com.hedvig.paymentservice.services.segmentPublisher

import com.segment.analytics.Analytics
import com.segment.analytics.messages.MessageBuilder
import com.segment.analytics.messages.TrackMessage

fun Analytics.identify(traitsMap: Map<String, Any>, memberId: String, integrations:Map<String,Boolean> = mapOf()) {
  val message = com.segment.analytics.messages.IdentifyMessage.builder()
    .userId(memberId)
    .traits(traitsMap)
  integrations.forEach { key, value -> message.enableIntegration(key, value) }

  this.enqueue(
    message)
}


fun Analytics.track(eventName: String, properties: Map<String, Any>, memberId: String, integrations:Map<String,Boolean> = mapOf()) {
  val message = TrackMessage
    .builder(eventName)
    .userId(memberId)
    .properties(properties)
  integrations.forEach { key, value -> message.enableIntegration(key, value) }

  this.enqueue(message)
}
