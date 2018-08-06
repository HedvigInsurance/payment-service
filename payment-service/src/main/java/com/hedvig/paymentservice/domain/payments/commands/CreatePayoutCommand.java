package com.hedvig.paymentservice.domain.payments.commands;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import javax.money.MonetaryAmount;
import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class CreatePayoutCommand {
  @TargetAggregateIdentifier String memberId;

  UUID transactionId;
  MonetaryAmount amount;
  String address;
  String countryCode;
  LocalDate dateOfBirth;
  String firstName;
  String lastName;
  Instant timestamp;
}
