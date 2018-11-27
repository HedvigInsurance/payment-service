package com.hedvig.paymentservice.domain.payments.commands;

import java.util.UUID;
import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class UpdateTrustlyAccountCommand {
  @TargetAggregateIdentifier String memberId;

  UUID hedvigOrderId;

  String accountId;
  String address;
  String bank;
  String city;
  String clearingHouse;
  String descriptor;
  boolean directDebitMandateActive;
  String lastDigits;
  String name;
  String personId;
  String zipCode;
}
