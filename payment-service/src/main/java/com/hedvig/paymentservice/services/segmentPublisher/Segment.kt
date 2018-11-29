package com.hedvig.paymentservice.services.segmentPublisher

import com.segment.analytics.Analytics
import com.segment.analytics.messages.TrackMessage

fun Analytics.identify(traitsMap: Map<String, Any>, memberId: String) {
  this.enqueue(
    com.segment.analytics.messages.IdentifyMessage.builder()
      .userId(memberId)
      .enableIntegration("All", false)
      .enableIntegration("Customer.io", true)
      .traits(traitsMap))
}


fun Analytics.track(eventName: String, properties: Map<String, Any>, memberId: String) {
  val message = TrackMessage
    .builder(eventName)
    .userId(memberId)
    .properties(properties)
    .enableIntegration("All", false)
    .enableIntegration("Customer.io", true)
  this.enqueue(message)
}
