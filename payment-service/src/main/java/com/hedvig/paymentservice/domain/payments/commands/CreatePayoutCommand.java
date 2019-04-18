package com.hedvig.paymentservice.domain.payments.commands;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import javax.money.MonetaryAmount;

import com.hedvig.paymentservice.domain.payments.TransactionCategory;
import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class CreatePayoutCommand {
  @TargetAggregateIdentifier String memberId;
  String address;
  String countryCode;
  LocalDate dateOfBirth;
  String firstName;
  String lastName;
  UUID transactionId;
  MonetaryAmount amount;
  Instant timestamp;
  TransactionCategory category;
  String referenceId;
  String note;
}
