package com.hedvig.paymentservice.query.adyenTransaction.entities;

import com.hedvig.paymentservice.domain.adyenTransaction.enums.AdyenTransactionStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.Instant;
import java.util.UUID;

@Entity
public class AdyenTransaction {
  @Id
  UUID transactionId;
  String memberId;
  String recurringDetailReference;
  @Enumerated(EnumType.STRING)
  AdyenTransactionStatus transactionStatus;
  @CreationTimestamp
  Instant createdAt;
  @UpdateTimestamp
  Instant updatedAt;

  public AdyenTransaction() {
  }

  @java.beans.ConstructorProperties({"transactionId", "memberId", "recurringDetailReference", "transactionStatus"})
  public AdyenTransaction(UUID transactionId, String memberId, String recurringDetailReference, AdyenTransactionStatus transactionStatus) {
    this.transactionId = transactionId;
    this.memberId = memberId;
    this.recurringDetailReference = recurringDetailReference;
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

  public AdyenTransactionStatus getTransactionStatus() {
    return this.transactionStatus;
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

  public void setTransactionStatus(AdyenTransactionStatus transactionStatus) {
    this.transactionStatus = transactionStatus;
  }

  public Instant getCreatedAt() {
    return this.createdAt;
  }

  public Instant getUpdatedAt() {
    return this.updatedAt;
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
    final Object this$transactionStatus = this.getTransactionStatus();
    final Object other$transactionStatus = other.getTransactionStatus();
    if (this$transactionStatus == null ? other$transactionStatus != null : !this$transactionStatus.equals(other$transactionStatus))
      return false;
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
    final Object $transactionStatus = this.getTransactionStatus();
    result = result * PRIME + ($transactionStatus == null ? 43 : $transactionStatus.hashCode());
    return result;
  }

  public String toString() {
    return "AdyenTransaction(transactionId=" + this.getTransactionId() + ", memberId=" + this.getMemberId() + ", recurringDetailReference=" + this.getRecurringDetailReference() + ", transactionStatus=" + this.getTransactionStatus() + ")";
  }
}
