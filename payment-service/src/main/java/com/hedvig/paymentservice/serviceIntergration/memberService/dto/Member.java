package com.hedvig.paymentservice.serviceIntergration.memberService.dto;

import java.time.LocalDate;
import lombok.Value;

@Value
public class Member {

  private String memberId;

  private String firstName;
  private String lastName;

  private LocalDate birthDate;

  private String street;
  private String city;
  private String zipCode;
  private String country;
}
