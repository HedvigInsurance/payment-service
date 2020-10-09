package com.hedvig.paymentservice.domain.adyenTokenRegistration.events.upcasters

import com.hedvig.paymentservice.configuration.MerchantAccounts
import com.hedvig.paymentservice.domain.adyenTokenRegistration.events.PendingAdyenTokenRegistrationCreatedEvent
import org.axonframework.serialization.SimpleSerializedType
import org.axonframework.serialization.upcasting.event.IntermediateEventRepresentation
import org.axonframework.serialization.upcasting.event.SingleEventUpcaster
import org.dom4j.Document

class PendingAdyenTokenRegistrationCreatedEventUpcaster(
  val merchantAccounts: MerchantAccounts
) : SingleEventUpcaster() {
  override fun canUpcast(intermediateRepresentation: IntermediateEventRepresentation): Boolean {
    val initialEvent = SimpleSerializedType(PendingAdyenTokenRegistrationCreatedEvent::class.java.typeName, null)
    val firstRevision = SimpleSerializedType(PendingAdyenTokenRegistrationCreatedEvent::class.java.typeName, "1.0")

    return (intermediateRepresentation.type == initialEvent && intermediateRepresentation.type.revision == null) ||
      (intermediateRepresentation.type == firstRevision && intermediateRepresentation.type.revision == firstRevision.revision)
  }

  override fun doUpcast(intermediateRepresentation: IntermediateEventRepresentation): IntermediateEventRepresentation {
    return intermediateRepresentation.upcastPayload(
      SimpleSerializedType(
        PendingAdyenTokenRegistrationCreatedEvent::class.java.typeName,
        "1.0"
      ),
      Document::class.java
    ) { document: Document ->
      val root = document.rootElement
      root.addElement("adyenMerchantAccount").text = merchantAccounts.merchantAccounts!!["NORWAY"]
      document
    }
  }
}
