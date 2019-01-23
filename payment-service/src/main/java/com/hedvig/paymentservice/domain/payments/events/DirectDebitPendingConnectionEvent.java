package com.hedvig.paymentservice.domain.payments.events;

import lombok.Value;

@Value
public class DirectDebitPendingConnectionEvent {

  String memberId;
  String trustlyAccountId;
}
