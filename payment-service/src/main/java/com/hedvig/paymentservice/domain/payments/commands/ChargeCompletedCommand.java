package com.hedvig.paymentservice.domain.payments.commands;

import java.time.Instant;
import java.util.UUID;
import javax.money.MonetaryAmount;
import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class ChargeCompletedCommand {
  @TargetAggregateIdentifier String memberId;

  UUID transactionId;
  MonetaryAmount amount;
  Instant timestamp;
}
