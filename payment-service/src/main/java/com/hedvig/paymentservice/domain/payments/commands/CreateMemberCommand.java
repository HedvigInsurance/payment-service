package com.hedvig.paymentservice.domain.payments.commands;

import org.axonframework.commandhandling.TargetAggregateIdentifier;

import lombok.Value;

@Value
public class CreateMemberCommand {
    @TargetAggregateIdentifier
    String memberId;
}
