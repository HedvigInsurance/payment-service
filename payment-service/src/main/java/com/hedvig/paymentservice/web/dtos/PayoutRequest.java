package com.hedvig.paymentservice.web.dtos;

import java.time.LocalDate;
import javax.money.MonetaryAmount;
import lombok.Value;

@Value
public class PayoutRequest {
  MonetaryAmount amount;
  String address;
  String countryCode;
  LocalDate dateOfBirth;
  String firstName;
  String lastName;
}
