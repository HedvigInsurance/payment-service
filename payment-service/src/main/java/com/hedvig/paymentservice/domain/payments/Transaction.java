package com.hedvig.paymentservice.domain.payments;

import java.time.Instant;
import java.util.UUID;
import javax.money.MonetaryAmount;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Transaction {
  UUID transactionId;

  MonetaryAmount amount;
  Instant timestamp;
  TransactionType transactionType;
  TransactionStatus transactionStatus;
}
