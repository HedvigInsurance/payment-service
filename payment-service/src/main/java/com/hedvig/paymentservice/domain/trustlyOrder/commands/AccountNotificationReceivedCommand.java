package com.hedvig.paymentservice.domain.trustlyOrder.commands;

import com.hedvig.paymentservice.domain.trustlyOrder.events.AccountNotificationReceivedEvent;
import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class AccountNotificationReceivedCommand {

    @TargetAggregateIdentifier
    String accountId;

    AccountNotificationReceivedEvent event;
}
