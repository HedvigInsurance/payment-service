package com.hedvig.paymentservice.graphQl.types;

public enum CancelDirectDebitStatus {
  ACCEPTED,
  DECLINED_MISSING_TOKEN,
  DECLINED_MISSING_REQUEST
}
