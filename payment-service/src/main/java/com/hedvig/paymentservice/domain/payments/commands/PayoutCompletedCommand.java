package com.hedvig.paymentservice.domain.payments.commands;

import java.time.Instant;
import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;

import org.axonframework.commandhandling.model.AggregateIdentifier;

import lombok.Value;

@Value
public class PayoutCompletedCommand {
    @AggregateIdentifier
    String memberId;

    MonetaryAmount amount;
    CurrencyUnit currency;
    Instant timestamp;
}
