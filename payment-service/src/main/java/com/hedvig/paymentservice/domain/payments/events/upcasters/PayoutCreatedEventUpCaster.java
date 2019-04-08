package com.hedvig.paymentservice.domain.payments.events.upcasters;

import com.hedvig.paymentservice.domain.payments.TransactionCategory;
import com.hedvig.paymentservice.domain.payments.events.PayoutCreatedEvent;
import org.axonframework.serialization.SimpleSerializedType;
import org.axonframework.serialization.upcasting.event.IntermediateEventRepresentation;
import org.axonframework.serialization.upcasting.event.SingleEventUpcaster;

public class PayoutCreatedEventUpCaster extends SingleEventUpcaster {

  private static SimpleSerializedType targetType = new SimpleSerializedType(
    PayoutCreatedEvent.class.getTypeName(), null
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
        "1.0"),
        org.dom4j.Document.class,
        document -> {
          document.getRootElement().addElement("category").setText(TransactionCategory.CLAIM.name());
          return document;
        }
      );
  }
}
