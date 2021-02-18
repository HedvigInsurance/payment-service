package com.hedvig.paymentservice.domain.payments.events.upcasters

import com.hedvig.paymentservice.domain.payments.events.PayoutCreatedEvent
import com.hedvig.paymentservice.domain.payments.events.PayoutDetails
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
import org.junit.Test
import org.springframework.test.util.ReflectionTestUtils

class PayoutUpcasterTest {

    @Test
    fun `upcast trustly PayoutCreatedEvent v2 to v3`() {

        val serializer = XStreamSerializer()
        val metaData = MetaData.with("key", "value")
        val eventData = DomainEventEntry(
            GenericDomainEventMessage(
                "test", "aggregateId", 0,
                "The payload will be replaced", metaData
            ),
            serializer
        )

        ReflectionTestUtils.setField(eventData, "payload", trustlyV2PayoutPayload.toByteArray())
        ReflectionTestUtils.setField(eventData, "payloadType", "com.hedvig.paymentservice.domain.payments.events.PayoutCreatedEvent")
        ReflectionTestUtils.setField(eventData, "payloadRevision", "2.0")


        val result = PayoutCreatedEventV3UpCaster().upcast(Stream.of(InitialEventRepresentation(eventData, serializer))).collect(toList())
        assertFalse(result.isEmpty())

        val firstEvent = result.get(0)
        assertEquals("3.0", firstEvent.getType().getRevision())

        val upcastedEvent =
            serializer.deserialize<PayoutCreatedEvent, PayoutCreatedEvent>(firstEvent.getData() as SerializedObject<PayoutCreatedEvent>)

        assertEquals("123", upcastedEvent.memberId)
        assert(upcastedEvent.payoutDetails is PayoutDetails.Trustly)
        assertEquals("TrustlyAccountId", (upcastedEvent.payoutDetails as PayoutDetails.Trustly).accountId)

        assertEquals(eventData.eventIdentifier, firstEvent.messageIdentifier)
        assertEquals(eventData.timestamp, firstEvent.timestamp)
        assertEquals(eventData.timestamp, firstEvent.timestamp)

        assertEquals(metaData, firstEvent.metaData.getObject())
    }

    @Test
    fun `upcast adyen PayoutCreatedEvent v2 to v3`() {

        val serializer = XStreamSerializer()
        val metaData = MetaData.with("key", "value")
        val eventData = DomainEventEntry(
            GenericDomainEventMessage(
                "test", "aggregateId", 0,
                "The payload will be replaced", metaData
            ),
            serializer
        )

        ReflectionTestUtils.setField(eventData, "payload", adyenV2PayoutPayload.toByteArray())
        ReflectionTestUtils.setField(eventData, "payloadType", "com.hedvig.paymentservice.domain.payments.events.PayoutCreatedEvent")
        ReflectionTestUtils.setField(eventData, "payloadRevision", "2.0")

        val result = PayoutCreatedEventV3UpCaster().upcast(Stream.of(InitialEventRepresentation(eventData, serializer))).collect(toList())
        assertFalse(result.isEmpty())

        val firstEvent = result.get(0)
        assertEquals("3.0", firstEvent.getType().getRevision())

        val upcastedEvent =
            serializer.deserialize<PayoutCreatedEvent, PayoutCreatedEvent>(firstEvent.getData() as SerializedObject<PayoutCreatedEvent>)

        assertEquals("adyen", upcastedEvent.memberId)
        assert(upcastedEvent.payoutDetails is PayoutDetails.Adyen)
        assertEquals("adyenShopperReference123", (upcastedEvent.payoutDetails as PayoutDetails.Adyen).shopperReference)

        assertEquals(eventData.eventIdentifier, firstEvent.messageIdentifier)
        assertEquals(eventData.timestamp, firstEvent.timestamp)
        assertEquals(eventData.timestamp, firstEvent.timestamp)

        assertEquals(metaData, firstEvent.metaData.getObject())
    }

    private val trustlyV2PayoutPayload = "<com.hedvig.paymentservice.domain.payments.events.PayoutCreatedEvent><memberId>123</memberId><transactionId>7d762438-5d14-434e-851a-3833a3a92b9e</transactionId><amount class=\"org.javamoney.moneta.Money\"><currency class=\"org.javamoney.moneta.internal.JDKCurrencyAdapter\"><baseCurrency>SEK</baseCurrency><context><data><entry><string>provider</string><string>java.util.Currency</string></entry></data></context></currency><monetaryContext><data><entry><string>amountType</string><java-class>org.javamoney.moneta.Money</java-class></entry><entry><string>java.lang.Class</string><java-class>org.javamoney.moneta.Money</java-class></entry><entry><string>precision</string><int>256</int></entry><entry><string>java.math.RoundingMode</string><java.math.RoundingMode>HALF_EVEN</java.math.RoundingMode></entry></data></monetaryContext><number>12</number></amount><address>street</address><countryCode>SE</countryCode><dateOfBirth>2020-01-01</dateOfBirth><firstName>first</firstName><lastName>last</lastName><timestamp>2021-02-17T09:32:33.809126Z</timestamp><trustlyAccountId>TrustlyAccountId</trustlyAccountId><category>CLAIM</category><referenceId>ref</referenceId><note>note</note><email>em@i.l</email><carrier>HDI</carrier></com.hedvig.paymentservice.domain.payments.events.PayoutCreatedEvent>"
    private val adyenV2PayoutPayload = "<com.hedvig.paymentservice.domain.payments.events.PayoutCreatedEvent><memberId>adyen</memberId><transactionId>0e454f33-5660-499f-9db6-17915274be7f</transactionId><amount class=\"org.javamoney.moneta.Money\"><currency class=\"org.javamoney.moneta.internal.JDKCurrencyAdapter\"><baseCurrency>SEK</baseCurrency><context><data><entry><string>provider</string><string>java.util.Currency</string></entry></data></context></currency><monetaryContext><data><entry><string>amountType</string><java-class>org.javamoney.moneta.Money</java-class></entry><entry><string>java.lang.Class</string><java-class>org.javamoney.moneta.Money</java-class></entry><entry><string>precision</string><int>256</int></entry><entry><string>java.math.RoundingMode</string><java.math.RoundingMode>HALF_EVEN</java.math.RoundingMode></entry></data></monetaryContext><number>12</number></amount><address>street</address><countryCode>SE</countryCode><dateOfBirth>2020-01-01</dateOfBirth><firstName>first</firstName><lastName>last</lastName><timestamp>2021-02-17T09:48:17.185539Z</timestamp><category>CLAIM</category><referenceId>ref</referenceId><note>note</note><adyenShopperReference>adyenShopperReference123</adyenShopperReference><email>em@i.l</email><carrier>HDI</carrier></com.hedvig.paymentservice.domain.payments.events.PayoutCreatedEvent>"
}
