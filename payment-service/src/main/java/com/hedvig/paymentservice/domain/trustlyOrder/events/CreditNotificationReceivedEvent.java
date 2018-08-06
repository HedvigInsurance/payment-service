package com.hedvig.paymentservice.domain.trustlyOrder.events;

import com.hedvig.paymentservice.domain.trustlyOrder.OrderType;
import java.time.Instant;
import java.util.UUID;
import javax.money.MonetaryAmount;
import lombok.Value;

@Value
public class CreditNotificationReceivedEvent {
  UUID hedvigOrderId;

  UUID transactionId;
  String notificationId;
  String trustlyOrderId;
  String memberId;
  MonetaryAmount amount;
  Instant timestamp;
  OrderType orderType;
}
