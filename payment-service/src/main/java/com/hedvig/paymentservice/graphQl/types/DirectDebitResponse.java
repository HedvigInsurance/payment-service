package com.hedvig.paymentservice.graphQl.types;

import lombok.Value;

@Value
public class DirectDebitResponse {
  String url;
  String orderId;

  public static DirectDebitResponse fromDirectDebitResposne(com.hedvig.paymentservice.web.dtos.DirectDebitResponse response) {
    return new DirectDebitResponse(response.getUrl(), response.getOrderId());
  }
}
