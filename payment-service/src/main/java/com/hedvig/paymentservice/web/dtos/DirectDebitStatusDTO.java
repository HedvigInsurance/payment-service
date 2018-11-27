package com.hedvig.paymentservice.web.dtos;

import lombok.Value;

@Value
public class DirectDebitStatusDTO {
  String memberId;
  Boolean directDebitActivated;
}
