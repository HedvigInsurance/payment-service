package com.hedvig.paymentservice.domain.payments.commands;

import java.util.UUID;
import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class ChargeFailedCommand {
  @TargetAggregateIdentifier String memberId;

  UUID transactionId;
}
