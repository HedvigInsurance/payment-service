package com.hedvig.paymentservice.domain.payments.events;

import com.hedvig.paymentservice.domain.payments.commands.UpdateTrustlyAccountCommand;
import lombok.Value;
import org.axonframework.commandhandling.model.AggregateIdentifier;

import java.util.UUID;

@Value
public class TrustlyAccountUpdatedEvent {
  @AggregateIdentifier
  String memberId;

  UUID hedvigOrderId;
  String trustlyAccountId;

  String address;
  String bank;
  String city;
  String clearingHouse;
  String descriptor;
  String lastDigits;
  String name;
  String personId;
  String zipCode;

  public static TrustlyAccountUpdatedEvent fromUpdateTrustlyAccountCmd(String memberId, UpdateTrustlyAccountCommand cmd) {
    return
      new TrustlyAccountUpdatedEvent(
        memberId,
        cmd.getHedvigOrderId(),
        cmd.getAccountId(),
        cmd.getAddress(),
        cmd.getBank(),
        cmd.getCity(),
        cmd.getClearingHouse(),
        cmd.getDescriptor(),
        cmd.getLastDigits(),
        cmd.getName(),
        cmd.getPersonId(),
        cmd.getZipCode());
  }

}
