package com.hedvig.paymentservice.domain.payments.events;

import lombok.Value;

@Value
public class DirectDebitDisconnectedEvent {

  String memberId;
  String trustlyAccountId;
}
