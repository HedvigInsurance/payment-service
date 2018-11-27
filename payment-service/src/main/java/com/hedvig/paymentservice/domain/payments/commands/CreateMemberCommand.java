package com.hedvig.paymentservice.domain.payments.commands;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class CreateMemberCommand {
  @TargetAggregateIdentifier String memberId;
}
