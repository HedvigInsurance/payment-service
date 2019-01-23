package com.hedvig.paymentservice.domain.payments.events;

import lombok.Value;

@Value
public class DirectDebitConnectedEvent {

  String memberId;
  String hedvigOrderId;
  String trustlyAccountId;
}
