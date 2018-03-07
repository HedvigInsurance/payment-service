package com.hedvig.paymentservice.domain.payments;

import java.time.Instant;
import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;

import lombok.Value;

@Value
public class Transaction {
    String transactionId;

    CurrencyUnit currency;
    MonetaryAmount amount;
    Instant timestamp;
    TransactionType transactionType;
    TransactionStatus transactionStatus;
}
