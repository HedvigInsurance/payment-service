package com.hedvig.paymentservice.domain.trustlyOrder.commands;

import com.hedvig.paymentService.trustly.data.notification.Notification;
import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

import java.util.UUID;

@Value
public class NotificationReceivedCommand {
    @TargetAggregateIdentifier UUID hedvigOrderId;

    Notification notification;

}
