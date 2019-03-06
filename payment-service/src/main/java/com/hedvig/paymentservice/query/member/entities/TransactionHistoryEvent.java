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

  @NotNull
  private UUID transactionId;

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

  public TransactionHistoryEvent(@NotNull final UUID transactionId, @NotNull final Instant time, @NotNull final TransactionHistoryEventType type) {
    this.id = UUID.randomUUID();
    this.transactionId = transactionId;
    this.time = time;
    this.type = type;
  }

  public TransactionHistoryEvent(@NotNull final UUID transactionId, @Nullable final BigDecimal amount, @Nullable final String currency, @NotNull final Instant time, @NotNull final TransactionHistoryEventType type, @Nullable final String reason) {
    this.id = UUID.randomUUID();
    this.transactionId = transactionId;
    this.amount = amount;
    this.currency = currency;
    this.time = time;
    this.type = type;
    this.reason = reason;
  }

  public UUID getId() {
    return id;
  }

  public UUID getTransactionId() {
    return transactionId;
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
