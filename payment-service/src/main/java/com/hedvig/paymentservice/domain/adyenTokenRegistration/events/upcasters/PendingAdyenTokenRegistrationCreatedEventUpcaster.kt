package com.hedvig.paymentservice.domain.adyenTokenRegistration.events.upcasters

import com.hedvig.paymentservice.domain.adyenTokenRegistration.events.PendingAdyenTokenRegistrationCreatedEvent
import org.axonframework.serialization.SimpleSerializedType
import org.axonframework.serialization.upcasting.event.IntermediateEventRepresentation
import org.axonframework.serialization.upcasting.event.SingleEventUpcaster
import org.dom4j.Document

class PendingAdyenTokenRegistrationCreatedEventUpcaster : SingleEventUpcaster() {
  private val targetType = SimpleSerializedType(PendingAdyenTokenRegistrationCreatedEvent::class.java.typeName, null)

  override fun canUpcast(intermediateRepresentation: IntermediateEventRepresentation): Boolean {
    return intermediateRepresentation.type == targetType
  }

  override fun doUpcast(
    intermediateRepresentation: IntermediateEventRepresentation): IntermediateEventRepresentation? {

    val memberId = intermediateRepresentation.getData(org.dom4j.Document::class.java)
      .data.rootElement.element("memberId")

    return intermediateRepresentation.upcastPayload(
        SimpleSerializedType(targetType.name, "2.0"),
      Document::class.java
    ) { document: Document ->
      document.rootElement
        .addElement("isPayoutSetup").data = false
      document.rootElement
        .addElement("shopperReference").text = memberId.text
      document
    }
  }
}
