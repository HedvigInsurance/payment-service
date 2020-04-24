package com.hedvig.paymentservice.query.adyenTransaction.entities;

import com.hedvig.paymentservice.domain.adyenTransaction.enums.AdyenTransactionStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.time.Instant;
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

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof AdyenTransaction)) return false;
    final AdyenTransaction other = (AdyenTransaction) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$transactionId = this.getTransactionId();
    final Object other$transactionId = other.getTransactionId();
    if (this$transactionId == null ? other$transactionId != null : !this$transactionId.equals(other$transactionId))
      return false;
    final Object this$memberId = this.getMemberId();
    final Object other$memberId = other.getMemberId();
    if (this$memberId == null ? other$memberId != null : !this$memberId.equals(other$memberId)) return false;
    final Object this$recurringDetailReference = this.getRecurringDetailReference();
    final Object other$recurringDetailReference = other.getRecurringDetailReference();
    if (this$recurringDetailReference == null ? other$recurringDetailReference != null : !this$recurringDetailReference.equals(other$recurringDetailReference))
      return false;
    final Object this$amount = this.getAmount();
    final Object other$amount = other.getAmount();
    if (this$amount == null ? other$amount != null : !this$amount.equals(other$amount)) return false;
    final Object this$currency = this.getCurrency();
    final Object other$currency = other.getCurrency();
    if (this$currency == null ? other$currency != null : !this$currency.equals(other$currency)) return false;
    final Object this$transactionStatus = this.getTransactionStatus();
    final Object other$transactionStatus = other.getTransactionStatus();
    if (this$transactionStatus == null ? other$transactionStatus != null : !this$transactionStatus.equals(other$transactionStatus))
      return false;
    final Object this$createdAt = this.getCreatedAt();
    final Object other$createdAt = other.getCreatedAt();
    if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt)) return false;
    final Object this$updatedAt = this.getUpdatedAt();
    final Object other$updatedAt = other.getUpdatedAt();
    if (this$updatedAt == null ? other$updatedAt != null : !this$updatedAt.equals(other$updatedAt)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof AdyenTransaction;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $transactionId = this.getTransactionId();
    result = result * PRIME + ($transactionId == null ? 43 : $transactionId.hashCode());
    final Object $memberId = this.getMemberId();
    result = result * PRIME + ($memberId == null ? 43 : $memberId.hashCode());
    final Object $recurringDetailReference = this.getRecurringDetailReference();
    result = result * PRIME + ($recurringDetailReference == null ? 43 : $recurringDetailReference.hashCode());
    final Object $amount = this.getAmount();
    result = result * PRIME + ($amount == null ? 43 : $amount.hashCode());
    final Object $currency = this.getCurrency();
    result = result * PRIME + ($currency == null ? 43 : $currency.hashCode());
    final Object $transactionStatus = this.getTransactionStatus();
    result = result * PRIME + ($transactionStatus == null ? 43 : $transactionStatus.hashCode());
    final Object $createdAt = this.getCreatedAt();
    result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
    final Object $updatedAt = this.getUpdatedAt();
    result = result * PRIME + ($updatedAt == null ? 43 : $updatedAt.hashCode());
    return result;
  }

  public String toString() {
    return "AdyenTransaction(transactionId=" + this.getTransactionId() + ", memberId=" + this.getMemberId() + ", recurringDetailReference=" + this.getRecurringDetailReference() + ", amount=" + this.getAmount() + ", currency=" + this.getCurrency() + ", transactionStatus=" + this.getTransactionStatus() + ", createdAt=" + this.getCreatedAt() + ", updatedAt=" + this.getUpdatedAt() + ")";
  }
}
