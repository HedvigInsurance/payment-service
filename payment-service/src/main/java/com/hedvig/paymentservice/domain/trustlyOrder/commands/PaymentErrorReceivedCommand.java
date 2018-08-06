package com.hedvig.paymentservice.domain.trustlyOrder.commands;

import com.hedvig.paymentService.trustly.data.response.Error;
import java.util.UUID;
import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class PaymentErrorReceivedCommand {
  @TargetAggregateIdentifier UUID hedvigOrderId;

  Error error;
}
