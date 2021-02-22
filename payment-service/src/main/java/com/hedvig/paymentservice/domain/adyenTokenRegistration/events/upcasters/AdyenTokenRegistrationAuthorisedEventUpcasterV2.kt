package com.hedvig.paymentservice.domain.adyenTokenRegistration.events.upcasters

import com.hedvig.paymentservice.domain.adyenTokenRegistration.events.AdyenTokenRegistrationAuthorisedEvent
import org.axonframework.serialization.SimpleSerializedType
import org.axonframework.serialization.upcasting.event.IntermediateEventRepresentation
import org.axonframework.serialization.upcasting.event.SingleEventUpcaster
import org.dom4j.Document

class AdyenTokenRegistrationAuthorisedEventUpcasterV2 : SingleEventUpcaster() {
  private val targetType = SimpleSerializedType(AdyenTokenRegistrationAuthorisedEvent::class.java.typeName, "1.0")
  override fun canUpcast(intermediateRepresentation: IntermediateEventRepresentation): Boolean {
    return intermediateRepresentation.type == targetType
  }

  override fun doUpcast(
      intermediateRepresentation: IntermediateEventRepresentation
  ): IntermediateEventRepresentation? {
    val memberId = intermediateRepresentation.getData(org.dom4j.Document::class.java)
      .data.rootElement.element("memberId")
    return intermediateRepresentation.upcastPayload(
      SimpleSerializedType(targetType.name, "2.0"),
      Document::class.java
    ) { document: Document ->
      document.rootElement
        .addElement("isPayoutSetup").text = "false"
      document.rootElement
        .addElement("shopperReference").text = memberId.text
      document
    }
  }
}
