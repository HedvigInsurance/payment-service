package com.hedvig.paymentservice.domain.payments.commands;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class CreateTrustlyAccountCommand {
    @TargetAggregateIdentifier
    String memberId;

    String trustlyAccountId;
}
