package com.hedvig.paymentservice.query.adyenTransaction.entities;

import com.hedvig.paymentservice.domain.adyenTransaction.enums.AdyenTransactionStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.lang.Nullable;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
public class AdyenTransaction {
  @Id
  UUID transactionId;
  String memberId;
  String recurringDetailReference;
  BigDecimal amount;
  String currency;
  @Enumerated(EnumType.STRING)
  AdyenTransactionStatus transactionStatus;
  @Nullable
  String reason;
  @CreationTimestamp
  Instant createdAt;
  @UpdateTimestamp
  Instant updatedAt;

  @java.beans.ConstructorProperties({"transactionId", "memberId", "recurringDetailReference", "amount", "currency", "transactionStatus"})
  public AdyenTransaction(UUID transactionId, String memberId, String recurringDetailReference, BigDecimal amount, String currency, AdyenTransactionStatus transactionStatus) {
    this.transactionId = transactionId;
    this.memberId = memberId;
    this.recurringDetailReference = recurringDetailReference;
    this.amount = amount;
    this.currency = currency;
    this.transactionStatus = transactionStatus;
  }

  public AdyenTransaction() {
  }

  public UUID getTransactionId() {
    return this.transactionId;
  }

  public String getMemberId() {
    return this.memberId;
  }

  public String getRecurringDetailReference() {
    return this.recurringDetailReference;
  }

  public BigDecimal getAmount() {
    return this.amount;
  }

  public String getCurrency() {
    return this.currency;
  }

  public AdyenTransactionStatus getTransactionStatus() {
    return this.transactionStatus;
  }

  public Instant getCreatedAt() {
    return this.createdAt;
  }

  public Instant getUpdatedAt() {
    return this.updatedAt;
  }

  public void setTransactionId(UUID transactionId) {
    this.transactionId = transactionId;
  }

  public void setMemberId(String memberId) {
    this.memberId = memberId;
  }

  public void setRecurringDetailReference(String recurringDetailReference) {
    this.recurringDetailReference = recurringDetailReference;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public void setTransactionStatus(AdyenTransactionStatus transactionStatus) {
    this.transactionStatus = transactionStatus;
  }

  @Nullable
  public String getReason() {
    return reason;
  }

  public void setReason(@Nullable String reason) {
    this.reason = reason;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AdyenTransaction)) return false;
    AdyenTransaction that = (AdyenTransaction) o;
    return Objects.equals(getTransactionId(), that.getTransactionId()) &&
      Objects.equals(getMemberId(), that.getMemberId()) &&
      Objects.equals(getRecurringDetailReference(), that.getRecurringDetailReference()) &&
      Objects.equals(getAmount(), that.getAmount()) &&
      Objects.equals(getCurrency(), that.getCurrency()) &&
      getTransactionStatus() == that.getTransactionStatus() &&
      Objects.equals(getReason(), that.getReason()) &&
      Objects.equals(getCreatedAt(), that.getCreatedAt()) &&
      Objects.equals(getUpdatedAt(), that.getUpdatedAt());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getTransactionId(), getMemberId(), getRecurringDetailReference(), getAmount(), getCurrency(), getTransactionStatus(), getReason(), getCreatedAt(), getUpdatedAt());
  }
}
