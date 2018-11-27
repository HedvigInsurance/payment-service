package com.hedvig.paymentservice.domain.trustlyOrder.events;

import lombok.Value;

@Value
public class TrustlyAccountUpdatedEvent {
  String acconutId;
  String address;
  String bank;
  String city;
  String clearingHouse;
  String descriptor;
  boolean directDebitMandate;
  String lastDigits;
  String name;
  String personId;
  String zipCode;
}
