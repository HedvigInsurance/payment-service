package com.hedvig.paymentservice.domain.payments.events;

import lombok.Value;

import javax.money.MonetaryAmount;
import java.time.Instant;
import java.util.UUID;

@Value
public class ChargeErroredEvent {
  String memberId;
  UUID transactionId;
  MonetaryAmount amount;
  String reason;
  Instant timestamp;
}
