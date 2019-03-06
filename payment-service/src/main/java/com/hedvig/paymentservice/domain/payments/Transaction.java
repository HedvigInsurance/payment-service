package com.hedvig.paymentservice.domain.payments;

import java.math.BigDecimal;
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

  public com.hedvig.paymentservice.query.member.entities.Transaction toTransactionEntity() {
    final com.hedvig.paymentservice.query.member.entities.Transaction tx = new com.hedvig.paymentservice.query.member.entities.Transaction();

    tx.setId(this.transactionId);
    tx.setCurrency(this.amount.getCurrency().getCurrencyCode());
    tx.setAmount(this.amount.getNumber().numberValueExact(BigDecimal.class));
    tx.setTimestamp(this.timestamp);
    tx.setTransactionType(this.transactionType);
    tx.setTransactionStatus(this.transactionStatus);

    return tx;
  }
}
