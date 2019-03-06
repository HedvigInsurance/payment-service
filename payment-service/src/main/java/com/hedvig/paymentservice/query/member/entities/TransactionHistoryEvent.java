package com.hedvig.paymentservice.query.member.entities;

import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
public class TransactionHistoryEvent {
  @Id
  private UUID id;

  @ManyToOne(cascade = CascadeType.PERSIST)
  @NotNull
  private Transaction transaction;

  @Nullable
  private BigDecimal amount;

  @Nullable
  private String currency;

  @NotNull
  private Instant time;

  @Enumerated(EnumType.STRING)
  @NotNull
  private TransactionHistoryEventType type;

  @Nullable
  private String reason = null;

  public TransactionHistoryEvent() {
  }

  public TransactionHistoryEvent(@NotNull final Transaction transaction, @NotNull final Instant time, @NotNull final TransactionHistoryEventType type) {
    this.id = UUID.randomUUID();
    this.transaction = transaction;
    this.time = time;
    this.type = type;
  }

  public TransactionHistoryEvent(@NotNull final Transaction transaction, @Nullable final BigDecimal amount, @Nullable final String currency, @NotNull final Instant time, @NotNull final TransactionHistoryEventType type, @Nullable final String reason) {
    this.id = UUID.randomUUID();
    this.transaction = transaction;
    this.amount = amount;
    this.currency = currency;
    this.time = time;
    this.type = type;
    this.reason = reason;
  }

  public UUID getId() {
    return id;
  }

  public Transaction getTransaction() {
    return transaction;
  }

  public UUID getTransactionId() {
    return transaction.getId();
  }

  @Nullable
  public BigDecimal getAmount() {
    return amount;
  }

  @Nullable
  public String getCurrency() {
    return currency;
  }

  public Instant getTime() {
    return time;
  }

  public TransactionHistoryEventType getType() {
    return type;
  }

  @Nullable
  public String getReason() {
    return reason;
  }
}
