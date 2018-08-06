package com.hedvig.paymentservice.domain.trustlyOrder.commands;

import lombok.Value;

@Value
public class UpdateAccountCommand {
  String accountId;
  String address;
  String bank;
  String city;
  String clearingHouse;
  String descriptor;
  Boolean directDebitMandate;
  String lastDigits;
  String name;
  String personId;
  String zipCode;
}
