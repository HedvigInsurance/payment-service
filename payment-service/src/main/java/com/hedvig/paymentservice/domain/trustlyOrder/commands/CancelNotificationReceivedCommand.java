package com.hedvig.paymentservice.domain.trustlyOrder.commands;

import com.hedvig.paymentService.trustly.data.notification.Notification;
import lombok.Value;

import java.util.UUID;

@Value
public class CancelNotificationReceivedCommand {
    UUID hedvigOrderId;
    Notification notification;
}
