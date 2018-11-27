package com.hedvig.paymentservice.domain.payments;

import lombok.Value;

@Value
public class TrustlyAccount {
  String accountId;
  boolean directDebitMandateActive;
}
