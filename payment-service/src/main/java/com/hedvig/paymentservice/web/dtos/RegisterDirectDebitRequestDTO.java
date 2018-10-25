package com.hedvig.paymentservice.web.dtos;

import lombok.Value;

@Value
public class RegisterDirectDebitRequestDTO {
  String firstName;
  String lastName;
  String personalNumber;
}
