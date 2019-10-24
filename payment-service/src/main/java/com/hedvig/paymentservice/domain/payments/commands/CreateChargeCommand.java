package com.hedvig.paymentservice.domain.payments.commands;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

import javax.money.MonetaryAmount;
import java.time.Instant;
import java.util.UUID;

@Value
public class CreateChargeCommand {
  @TargetAggregateIdentifier String memberId;

  UUID transactionId;
  MonetaryAmount amount;
  Instant timestamp;
  String email;
  String createdBy;
}
