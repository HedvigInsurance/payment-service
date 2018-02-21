package com.hedvig.paymentservice.services.trustly;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

import java.util.UUID;

@Value
public class SelectAccountRequestFailedCommand {
    @TargetAggregateIdentifier
    private final UUID requestId;

    private String exceptionMessage;
}
