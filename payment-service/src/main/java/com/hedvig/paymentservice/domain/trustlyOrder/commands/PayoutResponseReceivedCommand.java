package com.hedvig.paymentservice.domain.trustlyOrder.commands;

import java.util.UUID;
import javax.money.MonetaryAmount;
import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class PayoutResponseReceivedCommand {
  @TargetAggregateIdentifier UUID hedvigOrderId;

  String trustlyOrderId;
  MonetaryAmount amount;
}
