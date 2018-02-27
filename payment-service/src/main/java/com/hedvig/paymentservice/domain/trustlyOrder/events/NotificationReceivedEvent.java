package com.hedvig.paymentservice.domain.trustlyOrder.events;

import lombok.Value;

import java.util.UUID;

@Value
public class NotificationReceivedEvent {
    UUID hedvigOrderId;
    String notificationId;
    String trustlyOrderId;
}
