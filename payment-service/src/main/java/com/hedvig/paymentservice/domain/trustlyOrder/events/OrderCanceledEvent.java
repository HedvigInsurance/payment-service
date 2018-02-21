package com.hedvig.paymentservice.domain.trustlyOrder.events;

import lombok.Value;

import java.util.UUID;

@Value
public class OrderCanceledEvent {
    UUID hedvigOrderId;
}
