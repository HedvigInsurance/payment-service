package com.hedvig.paymentservice.domain.trustlyOrder.events;

import lombok.Value;

import java.util.UUID;

@Value
public class OrderAssignedTrustlyIdEvent {
    UUID hedvigOrderId;
    String trustlyOrderId;
}
