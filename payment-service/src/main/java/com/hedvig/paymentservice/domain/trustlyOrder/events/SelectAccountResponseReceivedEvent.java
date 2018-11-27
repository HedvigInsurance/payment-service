package com.hedvig.paymentservice.domain.trustlyOrder.events;

import java.util.UUID;
import lombok.Value;

@Value
public class SelectAccountResponseReceivedEvent {

  UUID hedvigOrderId;
  String iframeUrl;
}
