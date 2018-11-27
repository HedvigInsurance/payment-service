package com.hedvig.paymentservice.domain.trustlyOrder.events;

import com.hedvig.paymentService.trustly.data.response.Error;
import java.util.UUID;
import lombok.Value;

@Value
public class PaymentErrorReceivedEvent {
  UUID hedvigOrderId;

  Error error;
}
