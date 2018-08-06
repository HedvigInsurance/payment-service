package com.hedvig.paymentservice.domain.trustlyOrder.events;

import java.time.Instant;
import java.util.UUID;
import javax.money.MonetaryAmount;
import lombok.Value;

@Value
public class PendingNotificationReceivedEvent {
  UUID hedvigOrderId;

  String notificationId;
  String trustlyOrderId;
  MonetaryAmount amount;
  String memberId;
  Instant timestamp;
}
