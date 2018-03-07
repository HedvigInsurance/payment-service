package com.hedvig.paymentservice.domain.trustlyOrder.commands;

import java.util.UUID;
import javax.money.MonetaryAmount;

import org.axonframework.commandhandling.model.AggregateIdentifier;

import lombok.Value;

@Value
public class CreatePaymentOrderCommand {
    @AggregateIdentifier
    UUID hedvigOrderId;

    UUID transactionId;
    String memberId;
    MonetaryAmount amount;
    String accountId;
}
