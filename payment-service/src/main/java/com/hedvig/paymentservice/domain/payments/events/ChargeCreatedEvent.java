package com.hedvig.paymentservice.domain.payments.events;

import lombok.Value;
import org.axonframework.serialization.Revision;

import javax.money.MonetaryAmount;
import java.time.Instant;
import java.util.UUID;

@Value
@Revision("1.0")
public class ChargeCreatedEvent {
  String memberId;
  UUID transactionId;
  MonetaryAmount amount;
  Instant timestamp;
  String accountId; //providerId
  //provider ENUM
  String email;
  String createdBy;
}
