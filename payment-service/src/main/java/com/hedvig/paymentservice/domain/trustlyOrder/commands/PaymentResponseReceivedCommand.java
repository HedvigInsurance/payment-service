package com.hedvig.paymentservice.domain.trustlyOrder.commands;

import java.util.UUID;
import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class PaymentResponseReceivedCommand {
  @TargetAggregateIdentifier UUID hedvigOrderId;

  String url;
  String trustlyOrderId;
}
