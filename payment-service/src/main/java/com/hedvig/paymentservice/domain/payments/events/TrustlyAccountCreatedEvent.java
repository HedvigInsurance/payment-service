package com.hedvig.paymentservice.domain.payments.events;

import org.axonframework.commandhandling.model.AggregateIdentifier;

import lombok.Value;

@Value
public class TrustlyAccountCreatedEvent {
    @AggregateIdentifier
    String memberId;

    String trustlyAccountId;
}
