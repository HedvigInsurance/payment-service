package com.hedvig.paymentservice.domain.payments.events.upcasters;

import com.hedvig.paymentservice.domain.payments.TransactionCategory;
import com.hedvig.paymentservice.domain.payments.enums.Carrier;
import com.hedvig.paymentservice.domain.payments.events.PayoutCreatedEvent;
import org.axonframework.serialization.SimpleSerializedType;
import org.axonframework.serialization.upcasting.event.IntermediateEventRepresentation;
import org.axonframework.serialization.upcasting.event.SingleEventUpcaster;
import org.dom4j.Element;

public class PayoutCreatedEventV2UpCaster extends SingleEventUpcaster {

    private static SimpleSerializedType targetType = new SimpleSerializedType(
        PayoutCreatedEvent.class.getTypeName(), "1.0"
    );

    @Override
    protected boolean canUpcast(IntermediateEventRepresentation intermediateEventRepresentation) {
        return intermediateEventRepresentation.getType().equals(targetType);
    }

    @Override
    protected IntermediateEventRepresentation doUpcast(IntermediateEventRepresentation intermediateEventRepresentation) {
        return intermediateEventRepresentation.upcastPayload(
            new SimpleSerializedType(
                targetType.getName(),
                "2.0"),
            org.dom4j.Document.class,
            document -> {
                Element element = document.getRootElement();
                if (element.element("category").getText().equals(TransactionCategory.CLAIM.name())) {
                    element.addElement("carrier").setText(Carrier.HDI.name());
                }
                return document;
            }
        );
    }
}
