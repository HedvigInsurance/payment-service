package com.hedvig.paymentservice.domain.trustlyOrder.commands;

import java.time.Instant;
import java.util.UUID;
import javax.money.MonetaryAmount;

import org.axonframework.commandhandling.TargetAggregateIdentifier;

import lombok.Value;

@Value
public class CreditNotificationReceivedCommand {
    @TargetAggregateIdentifier
    UUID hedvigOrderId;

    String notificationId;
    String trustlyOrderId;
    String memberId;
    MonetaryAmount amount;
    Instant timestamp;
}
