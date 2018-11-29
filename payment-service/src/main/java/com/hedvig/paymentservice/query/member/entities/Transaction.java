package com.hedvig.paymentservice.query.member.entities;

import com.hedvig.paymentservice.domain.payments.TransactionStatus;
import com.hedvig.paymentservice.domain.payments.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import javax.money.MonetaryAmount;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import org.javamoney.moneta.Money;

@Entity
public class Transaction {
  @Id UUID id;

  BigDecimal amount;
  String currency;
  Instant timestamp;

  @Enumerated(EnumType.STRING)
  TransactionType transactionType;

  @Enumerated(EnumType.STRING)
  TransactionStatus transactionStatus;

  public MonetaryAmount getMoney() {
    return Money.of(this.amount, this.currency);
  }

  public Transaction() {
  }

  public UUID getId() {
    return this.id;
  }

  public BigDecimal getAmount() {
    return this.amount;
  }

  public String getCurrency() {
    return this.currency;
  }

  public Instant getTimestamp() {
    return this.timestamp;
  }

  public TransactionType getTransactionType() {
    return this.transactionType;
  }

  public TransactionStatus getTransactionStatus() {
    return this.transactionStatus;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public void setTimestamp(Instant timestamp) {
    this.timestamp = timestamp;
  }

  public void setTransactionType(TransactionType transactionType) {
    this.transactionType = transactionType;
  }

  public void setTransactionStatus(TransactionStatus transactionStatus) {
    this.transactionStatus = transactionStatus;
  }

  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof Transaction)) {
      return false;
    }
    final Transaction other = (Transaction) o;
    if (!other.canEqual((Object) this)) {
      return false;
    }
    final Object this$id = this.getId();
    final Object other$id = other.getId();
    if (this$id == null ? other$id != null : !this$id.equals(other$id)) {
      return false;
    }
    final Object this$amount = this.getAmount();
    final Object other$amount = other.getAmount();
    if (this$amount == null ? other$amount != null : !this$amount.equals(other$amount)) {
      return false;
    }
    final Object this$currency = this.getCurrency();
    final Object other$currency = other.getCurrency();
    if (this$currency == null ? other$currency != null : !this$currency.equals(other$currency)) {
      return false;
    }
    final Object this$timestamp = this.getTimestamp();
    final Object other$timestamp = other.getTimestamp();
    if (this$timestamp == null ? other$timestamp != null
      : !this$timestamp.equals(other$timestamp)) {
      return false;
    }
    final Object this$transactionType = this.getTransactionType();
    final Object other$transactionType = other.getTransactionType();
    if (this$transactionType == null ? other$transactionType != null
      : !this$transactionType.equals(other$transactionType)) {
      return false;
    }
    final Object this$transactionStatus = this.getTransactionStatus();
    final Object other$transactionStatus = other.getTransactionStatus();
    if (this$transactionStatus == null ? other$transactionStatus != null
      : !this$transactionStatus.equals(other$transactionStatus)) {
      return false;
    }
    return true;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    final Object $amount = this.getAmount();
    result = result * PRIME + ($amount == null ? 43 : $amount.hashCode());
    final Object $currency = this.getCurrency();
    result = result * PRIME + ($currency == null ? 43 : $currency.hashCode());
    final Object $timestamp = this.getTimestamp();
    result = result * PRIME + ($timestamp == null ? 43 : $timestamp.hashCode());
    final Object $transactionType = this.getTransactionType();
    result = result * PRIME + ($transactionType == null ? 43 : $transactionType.hashCode());
    final Object $transactionStatus = this.getTransactionStatus();
    result = result * PRIME + ($transactionStatus == null ? 43 : $transactionStatus.hashCode());
    return result;
  }

  protected boolean canEqual(Object other) {
    return other instanceof Transaction;
  }

  public String toString() {
    return "Transaction(id=" + this.getId() + ", amount=" + this.getAmount() + ", currency=" + this
      .getCurrency() + ", timestamp=" + this.getTimestamp() + ", transactionType=" + this
      .getTransactionType() + ", transactionStatus=" + this.getTransactionStatus() + ")";
  }
}
