package com.hedvig.paymentservice.domain.trustlyOrder.commands;

import java.util.UUID;
import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class SelectAccountResponseReceivedCommand {
  @TargetAggregateIdentifier private final UUID hedvigOrderId;

  private final String iframeUrl;

  private final String trustlyOrderId;
}
