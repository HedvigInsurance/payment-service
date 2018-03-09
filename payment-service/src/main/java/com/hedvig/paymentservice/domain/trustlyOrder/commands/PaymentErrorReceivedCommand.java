package com.hedvig.paymentservice.domain.trustlyOrder.commands;

import java.util.UUID;

import com.hedvig.paymentService.trustly.data.response.Error;

import org.axonframework.commandhandling.TargetAggregateIdentifier;

import lombok.Value;

@Value
public class PaymentErrorReceivedCommand {
    @TargetAggregateIdentifier
    UUID hedvigOrderId;

    Error error;
}
