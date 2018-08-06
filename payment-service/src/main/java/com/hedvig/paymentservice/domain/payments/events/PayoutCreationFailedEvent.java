package com.hedvig.paymentservice.domain.payments.events;

import java.time.Instant;
import java.util.UUID;
import javax.money.MonetaryAmount;
import lombok.Value;

@Value
public class PayoutCreationFailedEvent {
  String memberId;

  UUID transactionId;
  MonetaryAmount amount;
  Instant timestamp;
}
