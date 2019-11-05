package com.hedvig.paymentservice.domain.payments.events;

import java.time.Instant;
import java.util.UUID;
import javax.money.MonetaryAmount;
import lombok.Value;

@Value
public class ChargeCompletedEvent {
  public String memberId;
  public UUID transactionId;
  public MonetaryAmount amount;
  public Instant timestamp;
}
