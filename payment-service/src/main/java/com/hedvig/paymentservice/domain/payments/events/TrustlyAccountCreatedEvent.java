package com.hedvig.paymentservice.domain.payments.events;

import lombok.Value;
import org.axonframework.commandhandling.model.AggregateIdentifier;

import java.util.UUID;

@Value()
public class TrustlyAccountCreatedEvent {
    @AggregateIdentifier
    String memberId;
    UUID hedvigOrderId;

    String trustlyAccountId;
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
