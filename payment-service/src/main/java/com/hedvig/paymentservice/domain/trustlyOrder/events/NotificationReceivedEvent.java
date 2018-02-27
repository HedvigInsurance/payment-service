package com.hedvig.paymentservice.domain.trustlyOrder.events;

import lombok.Value;

@Value
public class NotificationReceivedEvent {
    String notificationId;
    String trustlyOrderId;
}
