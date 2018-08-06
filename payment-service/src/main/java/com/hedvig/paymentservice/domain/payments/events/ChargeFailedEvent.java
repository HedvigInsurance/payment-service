package com.hedvig.paymentservice.domain.payments.events;

import java.util.UUID;
import lombok.Value;

@Value
public class ChargeFailedEvent {
  String memberId;
  UUID transactionId;
}
