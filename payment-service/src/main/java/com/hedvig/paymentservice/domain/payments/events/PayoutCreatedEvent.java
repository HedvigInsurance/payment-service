package com.hedvig.paymentservice.domain.payments.events;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import javax.money.MonetaryAmount;

import com.hedvig.paymentservice.domain.payments.TransactionCategory;
import lombok.Value;
import org.axonframework.serialization.Revision;

@Value
@Revision("1.0")
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
  TransactionCategory category;
  String trustlyAccountId;
}
