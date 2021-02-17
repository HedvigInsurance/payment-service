package com.hedvig.paymentservice.domain.payments.events.upcasters

import com.hedvig.paymentservice.domain.payments.TransactionCategory
import com.hedvig.paymentservice.domain.payments.enums.Carrier
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
            element.addElement("payoutHandler")
            val payoutHandler = element.element("payoutHandler")
            when {
                element.element("trustlyAccountId")?.text != null -> {
                    payoutHandler.addAttribute("class", "com.hedvig.paymentservice.domain.payments.events.PayoutHandler\$Trustly")
                    payoutHandler.addElement("accountId").text = element.element("trustlyAccountId").text
                }
                element.element("adyenShopperReference")?.text != null -> {
                    payoutHandler.addAttribute("class", "com.hedvig.paymentservice.domain.payments.events.PayoutHandler\$Adyen")
                    payoutHandler.addElement("shopperReference").text = element.element("adyenShopperReference").text
                }
            }
            element.remove(element.element("trustlyAccountId"))
            element.remove(element.element("adyenShopperReference"))

            document
        }
    }
}
