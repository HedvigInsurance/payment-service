package com.hedvig.paymentservice.domain.payments.commands;

import java.time.Instant;
import java.util.UUID;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

import lombok.Value;

@Value
public class PayoutCompletedCommand {
    @TargetAggregateIdentifier
    String memberId;

    UUID transactionId;
    Instant timestamp;
}
