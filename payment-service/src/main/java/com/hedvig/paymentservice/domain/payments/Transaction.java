package com.hedvig.paymentservice.domain.payments;

import java.time.Instant;
import java.util.UUID;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import lombok.Data;
import lombok.Value;

@Data
public class Transaction {
    UUID transactionId;

    CurrencyUnit currency;
    MonetaryAmount amount;
    Instant timestamp;
    TransactionType transactionType;
    TransactionStatus transactionStatus;
}
