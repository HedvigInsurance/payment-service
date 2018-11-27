package com.hedvig.paymentservice.domain.payments.events;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import javax.money.MonetaryAmount;
import lombok.Value;

@Value
public class PayoutCreatedEvent {
  String memberId;

  UUID transactionId;
  MonetaryAmount amount;
  String address;
  String countryCode;
  LocalDate dateOfBirth;
  String firstName;
  String lastName;
  Instant timestamp;
  String trustlyAccountId;
}
