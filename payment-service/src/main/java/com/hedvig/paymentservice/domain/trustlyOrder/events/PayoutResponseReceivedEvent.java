package com.hedvig.paymentservice.domain.trustlyOrder.events;

import java.util.UUID;
import javax.money.MonetaryAmount;
import lombok.Value;

@Value
public class PayoutResponseReceivedEvent {
  UUID hedvigOrderId;

  String memberId;
  MonetaryAmount amount;
  UUID transactionId;
}
