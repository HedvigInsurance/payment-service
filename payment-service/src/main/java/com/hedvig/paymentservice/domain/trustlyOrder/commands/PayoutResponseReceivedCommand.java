package com.hedvig.paymentservice.domain.trustlyOrder.commands;

import java.util.UUID;
import javax.money.MonetaryAmount;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

import lombok.Value;

@Value
public class PayoutResponseReceivedCommand {
    @TargetAggregateIdentifier
    UUID hedvigOrderId;

    String trustlyOrderId;
    MonetaryAmount amount;
}