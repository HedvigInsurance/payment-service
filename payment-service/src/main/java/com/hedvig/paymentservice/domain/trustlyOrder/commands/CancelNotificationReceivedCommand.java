package com.hedvig.paymentservice.domain.trustlyOrder.commands;

import java.util.UUID;
import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class CancelNotificationReceivedCommand {
  @TargetAggregateIdentifier UUID hedvigOrderId;

  String notificationId;
  String trustlyOrderId;
  String memberId;
}
