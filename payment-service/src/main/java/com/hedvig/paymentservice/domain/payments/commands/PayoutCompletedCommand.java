package com.hedvig.paymentservice.domain.payments.commands;

import java.time.Instant;
import java.util.UUID;

import javax.money.MonetaryAmount;

import org.axonframework.commandhandling.TargetAggregateIdentifier;

import lombok.Value;

@Value
public class PayoutCompletedCommand {
    @TargetAggregateIdentifier
    String memberId;

    UUID transactionId;
    MonetaryAmount amount;
    Instant timestamp;
}
