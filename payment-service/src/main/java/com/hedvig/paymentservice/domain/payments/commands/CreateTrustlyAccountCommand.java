package com.hedvig.paymentservice.domain.payments.commands;

import lombok.Value;
import org.axonframework.commandhandling.model.AggregateIdentifier;

@Value
public class CreateTrustlyAccountCommand {
    @AggregateIdentifier
    String memberId;

    String trustlyAccountId;
}
