package com.hedvig.paymentservice.domain.adyenTokenRegistration.events.upcasters

import com.hedvig.paymentservice.domain.adyenTokenRegistration.events.AdyenTokenRegistrationAuthorisedEvent
import org.axonframework.serialization.SimpleSerializedType
import org.axonframework.serialization.upcasting.event.IntermediateEventRepresentation
import org.axonframework.serialization.upcasting.event.SingleEventUpcaster
import org.dom4j.Document

class AdyenTokenRegistrationAuthorisedEventUpcaster(
) : SingleEventUpcaster() {
  override fun canUpcast(intermediateRepresentation: IntermediateEventRepresentation): Boolean {
    val initialEvent = SimpleSerializedType(AdyenTokenRegistrationAuthorisedEvent::class.java.typeName, null)
    return (intermediateRepresentation.type == initialEvent)
  }

  override fun doUpcast(intermediateRepresentation: IntermediateEventRepresentation): IntermediateEventRepresentation {
    return intermediateRepresentation.upcastPayload(
      SimpleSerializedType(
        AdyenTokenRegistrationAuthorisedEvent::class.java.typeName,
        "1.0"
      ),
      Document::class.java
    ) { document: Document ->
      val root = document.rootElement
      root.addElement("adyenMerchantAccount").text = "HedvigABCOM"
      document
    }
  }
}
