package com.hedvig.paymentservice.web.dtos;

import lombok.Value;

@Value
public class UrlResponse {
  String url;
  String orderId;
}
