package com.hedvig.paymentservice.services.trustly.dto;

import javax.money.MonetaryAmount;
import lombok.Value;

@Value
public class PaymentRequest {
  String memberId;
  MonetaryAmount amount;
  String accountId;
  String email;
}
