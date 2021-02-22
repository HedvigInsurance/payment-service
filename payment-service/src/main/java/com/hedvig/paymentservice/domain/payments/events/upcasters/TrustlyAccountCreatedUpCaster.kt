package com.hedvig.paymentservice.domain.payments.events.upcasters

import com.hedvig.paymentservice.domain.payments.events.DirectDebitConnectedEvent
import com.hedvig.paymentservice.domain.payments.events.DirectDebitDisconnectedEvent
import com.hedvig.paymentservice.domain.payments.events.DirectDebitPendingConnectionEvent
import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountCreatedEvent
import java.util.stream.Stream
import org.axonframework.serialization.SimpleSerializedType
import org.axonframework.serialization.upcasting.event.EventMultiUpcaster
import org.axonframework.serialization.upcasting.event.IntermediateEventRepresentation
import org.dom4j.Document

class TrustlyAccountCreatedUpCaster : EventMultiUpcaster() {

  override fun canUpcast(intermediateRepresentation: IntermediateEventRepresentation): Boolean {
    return intermediateRepresentation.type == eventTypes[TRUSTLY_ACCOUNT_CREATED] &&
      intermediateRepresentation.type.revision == eventTypes[TRUSTLY_ACCOUNT_CREATED]?.revision
  }

  override fun doUpcast(intermediateRepresentation: IntermediateEventRepresentation): Stream<IntermediateEventRepresentation> {
    val directDebit = intermediateRepresentation.getData(org.dom4j.Document::class.java)
      .data.rootElement.element("directDebitMandateActivated")
    return Stream.of(
      intermediateRepresentation.upcastPayload(
        eventTypes[TRUSTLY_ACCOUNT_CREATED_V1],
        org.dom4j.Document::class.java
      ) { document ->
        document.rootElement.remove(document.rootElement.element("directDebitMandateActivated"))
        document
      },
      convertToDirectDebitRepresentation(intermediateRepresentation, directDebit?.text?.toBoolean())
    )
  }

  private fun convertToDirectDebitRepresentation(
      intermediateRepresentation: IntermediateEventRepresentation,
      directDebit: Boolean?
  ): IntermediateEventRepresentation? {
    val eventType = when (directDebit) {
      true -> CONNECTED
      false -> DISCONNECTED
      else -> PENDING_CONNECTION
    }

    return intermediateRepresentation.upcastPayload(
      eventTypes[eventType],
      Document::class.java
    ) { document ->
      val root = document.rootElement
      root.name = eventTypes[eventType]!!.name
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
    }
  }

  companion object {
    private const val CONNECTED = "connected"
    private const val PENDING_CONNECTION = "pendingConnection"
    private const val DISCONNECTED = "disconnected"
    private const val TRUSTLY_ACCOUNT_CREATED = "trustlyAccountCreated"
    private const val TRUSTLY_ACCOUNT_CREATED_V1 = "trustlyAccountCreatedV1"

    private val eventTypes = mapOf(
      CONNECTED to Pair(DirectDebitConnectedEvent::class.java.typeName, null),
      PENDING_CONNECTION to Pair(DirectDebitPendingConnectionEvent::class.java.typeName, null),
      DISCONNECTED to Pair(DirectDebitDisconnectedEvent::class.java.typeName, null),
      TRUSTLY_ACCOUNT_CREATED to Pair(TrustlyAccountCreatedEvent::class.java.typeName, null),
      TRUSTLY_ACCOUNT_CREATED_V1 to Pair(TrustlyAccountCreatedEvent::class.java.typeName, "1.0")
    ).mapValues { SimpleSerializedType(it.value.first, it.value.second) }
  }
}
