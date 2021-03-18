package com.hedvig.paymentservice.domain.payments.events.upcasters

import com.hedvig.paymentservice.domain.payments.events.PayoutCreatedEvent
import org.axonframework.serialization.SimpleSerializedType
import org.axonframework.serialization.upcasting.event.IntermediateEventRepresentation
import org.axonframework.serialization.upcasting.event.SingleEventUpcaster
import org.dom4j.Document

class PayoutCreatedEventV3UpCaster : SingleEventUpcaster() {

    private val targetType = SimpleSerializedType(
        PayoutCreatedEvent::class.java.typeName, "2.0"
    )

    override fun canUpcast(intermediateEventRepresentation: IntermediateEventRepresentation) =
        intermediateEventRepresentation.type == targetType

    override fun doUpcast(intermediateEventRepresentation: IntermediateEventRepresentation): IntermediateEventRepresentation {
        return intermediateEventRepresentation.upcastPayload(
            SimpleSerializedType(
                targetType.name,
                "3.0"),
            Document::class.java
        ) { document: Document ->
            val element = document.rootElement
            element.addElement("payoutDetails")
            val payoutDetails = element.element("payoutDetails")
            when {
                element.element("trustlyAccountId")?.text != null -> {
                    payoutDetails.addAttribute("class", "com.hedvig.paymentservice.domain.payments.events.PayoutDetails\$Trustly")
                    payoutDetails.addElement("accountId").text = element.element("trustlyAccountId").text
                }
                element.element("adyenShopperReference")?.text != null -> {
                    payoutDetails.addAttribute("class", "com.hedvig.paymentservice.domain.payments.events.PayoutDetails\$Adyen")
                    payoutDetails.addElement("shopperReference").text = element.element("adyenShopperReference").text
                }
            }
            element.remove(element.element("trustlyAccountId"))
            element.remove(element.element("adyenShopperReference"))

            document
        }
    }
}
