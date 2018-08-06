package com.hedvig.paymentservice.web.dtos;

import lombok.Value;

@Value
public class DirectDebitResponse {
  String url;
  String orderId;
}
