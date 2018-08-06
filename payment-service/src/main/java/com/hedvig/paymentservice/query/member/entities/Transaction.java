package com.hedvig.paymentservice.query.member.entities;

import com.hedvig.paymentservice.domain.payments.TransactionStatus;
import com.hedvig.paymentservice.domain.payments.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Transaction {
  @Id UUID id;

  BigDecimal amount;
  String currency;
  Instant timestamp;

  @Enumerated(EnumType.STRING)
  TransactionType transactionType;

  @Enumerated(EnumType.STRING)
  TransactionStatus transactionStatus;
}
