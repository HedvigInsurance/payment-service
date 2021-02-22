package com.hedvig.paymentservice.domain.payments.events.upcasters

import com.hedvig.paymentservice.domain.payments.events.DirectDebitConnectedEvent
import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountCreatedEvent
import java.util.*
import java.util.stream.Collectors.toList
import java.util.stream.Stream
import org.axonframework.eventsourcing.GenericDomainEventMessage
import org.axonframework.eventsourcing.eventstore.jpa.DomainEventEntry
import org.axonframework.messaging.MetaData
import org.axonframework.serialization.SerializedObject
import org.axonframework.serialization.upcasting.event.InitialEventRepresentation
import org.axonframework.serialization.xml.XStreamSerializer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Ignore
import org.junit.Test

class TrustlyAccountCreatedUpCasterTest {

  @Test
  @Ignore("Just for documentation on how to test UpCasters")
  fun test() {

    val serializer = XStreamSerializer()
    val metaData = MetaData.with("key", "value")
    val eventData = DomainEventEntry(
      GenericDomainEventMessage(
        "test", "aggregateId", 0,
        TrustlyAccountCreatedEvent(
          "123",
          UUID.randomUUID(),
          "123",
          "address",
          "bank",
          "city",
          "clearingHouse",
          "descriptor",
          "listDigits",
          "name",
          "personId",
          "zipCode"
        ), metaData
      ),
      serializer
    )
    val upcaster = TrustlyAccountCreatedUpCaster()
    val result = upcaster.upcast(Stream.of(InitialEventRepresentation(eventData, serializer))).collect(toList())
    assertFalse(result.isEmpty())
    val firstEvent = result.get(0)
    assertEquals(null, firstEvent.getType().getRevision())
    val upcastedEvent =
      serializer.deserialize<DirectDebitConnectedEvent, DirectDebitConnectedEvent>(firstEvent.getData() as SerializedObject<DirectDebitConnectedEvent>)
    assertEquals("123", upcastedEvent.memberId)
    assertEquals(eventData.getEventIdentifier(), firstEvent.getMessageIdentifier())
    assertEquals(eventData.getTimestamp(), firstEvent.getTimestamp())
    assertEquals(metaData, firstEvent.getMetaData().getObject())
  }
}
