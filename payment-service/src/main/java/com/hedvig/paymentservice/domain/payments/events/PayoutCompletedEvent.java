package com.hedvig.paymentservice.domain.payments.events;

import java.time.Instant;
import java.util.UUID;
import lombok.Value;

@Value
public class PayoutCompletedEvent {
  String memberId;

  UUID transactionId;
  Instant timestamp;
}
