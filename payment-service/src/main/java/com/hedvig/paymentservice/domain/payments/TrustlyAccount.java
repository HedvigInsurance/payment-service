package com.hedvig.paymentservice.domain.payments;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TrustlyAccount {
  String accountId;
  DirectDebitStatus directDebitStatus;
}
