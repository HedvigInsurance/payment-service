package com.hedvig.paymentservice.services.trustly;

import java.util.UUID;
import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class SelectAccountRequestFailedCommand {
  @TargetAggregateIdentifier private final UUID requestId;

  private String exceptionMessage;
}
