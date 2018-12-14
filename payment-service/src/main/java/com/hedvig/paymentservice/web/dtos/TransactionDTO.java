package com.hedvig.paymentservice.web.dtos;

import com.hedvig.paymentservice.query.member.entities.Transaction;
import java.time.Instant;
import java.util.UUID;
import javax.money.MonetaryAmount;
import lombok.Value;

@Value
public class TransactionDTO {
  UUID id;
  MonetaryAmount amount;
  Instant timestamp;
  String transactionType;
  String transactionStatus;

  public static TransactionDTO fromTransaction(Transaction t){
    return new TransactionDTO(
        t.getId(),
        t.getMoney(),
        t.getTimestamp(),
        t.getTransactionType().toString(),
        t.getTransactionStatus().toString()
    );
  }
}