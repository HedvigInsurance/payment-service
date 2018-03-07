package com.hedvig.paymentservice.domain.payments.commands;

import org.axonframework.commandhandling.model.AggregateIdentifier;
import java.time.Instant;
import java.util.UUID;
import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;

import lombok.Value;

@Value
public class CreateChargeCommand {
    @AggregateIdentifier
    String memberId;

    UUID transactionId;
    MonetaryAmount amount;
    CurrencyUnit unit;
    Instant timestamp;
    String email;
}
