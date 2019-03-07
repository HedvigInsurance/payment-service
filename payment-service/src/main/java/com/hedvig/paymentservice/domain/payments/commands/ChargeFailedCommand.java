package com.hedvig.paymentservice.domain.payments.commands;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

import java.util.UUID;

@Value
public class ChargeFailedCommand {
  @TargetAggregateIdentifier
  String memberId;

  UUID transactionId;
}
