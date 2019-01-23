package com.hedvig.paymentservice.domain.payments.events.upcasters

import com.hedvig.paymentservice.domain.payments.events.DirectDebitConnectedEvent
import com.hedvig.paymentservice.domain.payments.events.DirectDebitDisconnectedEvent
import com.hedvig.paymentservice.domain.payments.events.DirectDebitPendingConnectionEvent
import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountCreatedEvent
import org.axonframework.serialization.SimpleSerializedType
import org.axonframework.serialization.upcasting.event.EventMultiUpcaster
import org.axonframework.serialization.upcasting.event.IntermediateEventRepresentation
import java.util.stream.Stream

class TrustlyAccountCreatedUpCaster : EventMultiUpcaster() {

  override fun canUpcast(intermediateRepresentation: IntermediateEventRepresentation): Boolean {
    return true //intermediateRepresentation.type == targetType
      //&& intermediateRepresentation.type.revision == targetType.revision
  }

  override fun doUpcast(intermediateRepresentation: IntermediateEventRepresentation): Stream<IntermediateEventRepresentation> {

    val data = intermediateRepresentation.getData(String::class.java)

    return Stream.of(

//        intermediateRepresentation.upcastPayload(
//          SimpleSerializedType(targetType.name, "1.0"),
//          org.dom4j.Document::class.java
//        ) { document ->
//          val status = document.rootElement.element("directDebitMandateActivated").text
//          if(status == null) {
//            return IntermediateEventRepresentation
//          }
//          document
//        },

      intermediateRepresentation.upcastPayload(
        ddTypes["connected"],
        org.dom4j.Document::class.java
      ) { document ->
        val root = document.rootElement
        root.name = ddTypes["connected"]!!.name
          root.remove(root.element("address"))
          root.remove(root.element("address"))
          root.remove(root.element("bank"))
          root.remove(root.element("city"))
          root.remove(root.element("clearingHouse"))
          root.remove(root.element("descriptor"))
          root.remove(root.element("lastDigits"))
          root.remove(root.element("name"))
          root.remove(root.element("personId"))
          root.remove(root.element("zipCode"))
        root.remove(root.element("directDebitMandateActivated"))
        document
      },
      intermediateRepresentation.upcastPayload(
        SimpleSerializedType(targetType.name, "1.0"),
        org.dom4j.Document::class.java
      ) { document ->
        document.rootElement.remove(document.rootElement.element("directDebitMandateActivated"))
        document
      }
    )
  }

  companion object {

    private val targetType = SimpleSerializedType(TrustlyAccountCreatedEvent::class.java.typeName, null)

    private val ddTypes = mapOf(
      "connected" to Pair(DirectDebitConnectedEvent::class.java.typeName, null),
      "pendingConnection" to Pair(DirectDebitPendingConnectionEvent::class.java.typeName, null),
      "disconnected" to Pair(DirectDebitDisconnectedEvent::class.java.typeName, null)
    )
      .mapValues { SimpleSerializedType(it.value.first, it.value.second) }


    private val getTargetType = SimpleSerializedType(DirectDebitConnectedEvent::class.java.typeName, "1.0")
  }

}
