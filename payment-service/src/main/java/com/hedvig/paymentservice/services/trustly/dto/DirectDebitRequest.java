package com.hedvig.paymentservice.services.trustly.dto;

import lombok.Value;

@Value
public class DirectDebitRequest {
  String firstName;
  String lastName;
  String ssn;
  String memberId;
  String triggerId;
}
