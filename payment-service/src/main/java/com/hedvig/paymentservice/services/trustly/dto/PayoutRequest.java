package com.hedvig.paymentservice.services.trustly.dto;

import java.time.LocalDate;
import javax.money.MonetaryAmount;
import lombok.Value;

@Value
public class PayoutRequest {
  String memberId;
  MonetaryAmount amount;
  String accountId;
  String address;
  String countryCode;
  LocalDate dateOfBirth;
  String firstName;
  String lastName;
}
