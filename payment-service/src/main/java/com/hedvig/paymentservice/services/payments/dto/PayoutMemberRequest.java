package com.hedvig.paymentservice.services.payments.dto;

import java.time.LocalDate;
import javax.money.MonetaryAmount;
import lombok.Value;

@Value
public class PayoutMemberRequest {
  String memberId;

  MonetaryAmount amount;
  String address;
  String countryCode;
  LocalDate dateOfBirth;
  String firstName;
  String lastName;
}
