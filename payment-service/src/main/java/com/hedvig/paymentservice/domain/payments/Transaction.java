package com.hedvig.paymentservice.domain.payments;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.money.MonetaryAmount;
import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class Transaction {
  UUID transactionId;

  MonetaryAmount amount;
  Instant timestamp;
  TransactionType transactionType;
  TransactionStatus transactionStatus;
}
