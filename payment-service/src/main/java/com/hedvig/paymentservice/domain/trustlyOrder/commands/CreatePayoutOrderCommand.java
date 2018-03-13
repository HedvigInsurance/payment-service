package com.hedvig.paymentservice.domain.trustlyOrder.commands;

import java.time.LocalDate;
import java.util.UUID;
import javax.money.MonetaryAmount;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

import lombok.Value;

@Value
public class CreatePayoutOrderCommand {
    @TargetAggregateIdentifier
    UUID hedvigOrderId;

    UUID transactionId;
    String memberId;
    MonetaryAmount amount;
    String trustlyAccountId;
    String address;
    String countryCode;
    LocalDate dateOfBirth;
    String firstName;
    String lastName;
}
