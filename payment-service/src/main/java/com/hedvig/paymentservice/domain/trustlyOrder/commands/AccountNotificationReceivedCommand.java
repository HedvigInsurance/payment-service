package com.hedvig.paymentservice.domain.trustlyOrder.commands;

import java.util.UUID;
import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class AccountNotificationReceivedCommand {
  @TargetAggregateIdentifier UUID hedvigOrderId;

  String notificationId;
  String trustlyOrderId;
  String accountId;
  String address;
  String bank;
  String city;
  String clearingHouse;
  String descriptor;
  boolean directDebitMandateActivated;
  String lastDigits;
  String name;
  String personId;
  String zipCode;
}
