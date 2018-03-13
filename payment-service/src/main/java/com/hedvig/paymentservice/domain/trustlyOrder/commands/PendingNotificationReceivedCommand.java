package com.hedvig.paymentservice.domain.trustlyOrder.commands;

import java.time.Instant;
import java.util.UUID;
import javax.money.MonetaryAmount;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

import lombok.Value;

@Value
public class PendingNotificationReceivedCommand {
    @TargetAggregateIdentifier
    UUID hedvigOrderId;

    String notificationId;
    String trustlyOrderId;
    MonetaryAmount amount;
    String memberId;
    Instant timestamp;
}
