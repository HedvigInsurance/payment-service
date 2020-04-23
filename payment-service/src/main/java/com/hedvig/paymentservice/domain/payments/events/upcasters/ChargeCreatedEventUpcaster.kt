package com.hedvig.paymentservice.domain.payments.events.upcasters

import com.hedvig.paymentservice.domain.payments.enums.PayinProvider
import com.hedvig.paymentservice.domain.payments.events.ChargeCreatedEvent
import org.axonframework.serialization.SimpleSerializedType
import org.axonframework.serialization.upcasting.event.EventMultiUpcaster
import org.axonframework.serialization.upcasting.event.IntermediateEventRepresentation
import org.dom4j.Document
import java.util.stream.Stream

class ChargeCreatedEventUpcaster : EventMultiUpcaster() {
  override fun canUpcast(intermediateRepresentation: IntermediateEventRepresentation): Boolean {
    return intermediateRepresentation.type == SimpleSerializedType(
      ChargeCreatedEvent::class.java.typeName,
      "1.0"
    ) && intermediateRepresentation.type.revision == "1.0"
  }

  override fun doUpcast(intermediateRepresentation: IntermediateEventRepresentation): Stream<IntermediateEventRepresentation> {
    val trustlyAccountId = intermediateRepresentation.getData(org.dom4j.Document::class.java)
      .data.rootElement.element("accountId")


    return Stream.of(intermediateRepresentation.upcastPayload(
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
    )
  }
}
