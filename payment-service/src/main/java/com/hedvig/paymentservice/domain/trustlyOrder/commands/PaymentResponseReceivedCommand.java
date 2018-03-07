package com.hedvig.paymentservice.domain.trustlyOrder.commands;

import java.util.UUID;

import org.axonframework.commandhandling.model.AggregateIdentifier;

import lombok.Value;

@Value
public class PaymentResponseReceivedCommand {
    @AggregateIdentifier
    UUID hedvigOrderId;

    String url;
    String trustlyOrderId;
}
