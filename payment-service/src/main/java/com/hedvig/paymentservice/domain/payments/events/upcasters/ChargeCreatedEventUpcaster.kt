package com.hedvig.paymentservice.domain.payments.events.upcasters

import com.hedvig.paymentservice.domain.payments.enums.PayinProvider
import com.hedvig.paymentservice.domain.payments.events.ChargeCreatedEvent
import org.axonframework.serialization.SimpleSerializedType
import org.axonframework.serialization.upcasting.event.IntermediateEventRepresentation
import org.axonframework.serialization.upcasting.event.SingleEventUpcaster
import org.dom4j.Document

class ChargeCreatedEventUpcaster : SingleEventUpcaster() {
  override fun canUpcast(intermediateRepresentation: IntermediateEventRepresentation): Boolean {
    val initialEvent = SimpleSerializedType(ChargeCreatedEvent::class.java.typeName, null)
    val firstRevision = SimpleSerializedType(ChargeCreatedEvent::class.java.typeName, "1.0")

    return (intermediateRepresentation.type == initialEvent && intermediateRepresentation.type.revision == null) ||
      (intermediateRepresentation.type == firstRevision && intermediateRepresentation.type.revision == firstRevision.revision)
  }

  override fun doUpcast(intermediateRepresentation: IntermediateEventRepresentation): IntermediateEventRepresentation {
    val trustlyAccountId = intermediateRepresentation.getData(org.dom4j.Document::class.java)
      .data.rootElement.element("accountId")


    return intermediateRepresentation.upcastPayload(
      SimpleSerializedType(
        ChargeCreatedEvent::class.java.typeName,
        "2.0"
      ),
      Document::class.java
    ) { document: Document ->
      val root = document.rootElement
      root.remove(root.element("accountId"))
      root.addElement("providerId").text = trustlyAccountId.text
      root.addElement("provider").text = PayinProvider.TRUSTLY.name
      document
    }
  }

}
