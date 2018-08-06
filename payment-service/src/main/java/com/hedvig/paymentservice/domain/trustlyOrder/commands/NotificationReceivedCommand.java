package com.hedvig.paymentservice.domain.trustlyOrder.commands;

import com.hedvig.paymentService.trustly.data.notification.Notification;
import java.util.UUID;
import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class NotificationReceivedCommand {
  @TargetAggregateIdentifier UUID hedvigOrderId;

  Notification notification;
}
