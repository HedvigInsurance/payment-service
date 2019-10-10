package com.hedvig.paymentservice.domain.payments.events;

import javax.money.MonetaryAmount;
import java.util.UUID;

public class ChargeFailedEvent {
  String memberId;
  UUID transactionId;
  MonetaryAmount amount;

  @java.beans.ConstructorProperties({"memberId", "transactionId"})
  public ChargeFailedEvent(String memberId, UUID transactionId, MonetaryAmount amount) {
    this.memberId = memberId;
    this.transactionId = transactionId;
    this.amount = amount;

  }

  public String getMemberId() {
    return this.memberId;
  }

  public UUID getTransactionId() {
    return this.transactionId;
  }

  public MonetaryAmount getAmount() { return this.amount; }

  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof ChargeFailedEvent)) {
      return false;
    }
    final ChargeFailedEvent other = (ChargeFailedEvent) o;
    final Object this$memberId = this.getMemberId();
    final Object other$memberId = other.getMemberId();
    if (this$memberId == null ? other$memberId != null : !this$memberId.equals(other$memberId)) {
      return false;
    }
    final Object this$transactionId = this.getTransactionId();
    final Object other$transactionId = other.getTransactionId();
    if (this$transactionId == null ? other$transactionId != null
      : !this$transactionId.equals(other$transactionId)) {
      return false;
    }
    return true;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $memberId = this.getMemberId();
    result = result * PRIME + ($memberId == null ? 43 : $memberId.hashCode());
    final Object $transactionId = this.getTransactionId();
    result = result * PRIME + ($transactionId == null ? 43 : $transactionId.hashCode());
    return result;
  }

  public String toString() {
    return "ChargeFailedEvent(memberId=" + this.getMemberId() + ", transactionId=" + this
      .getTransactionId() + ")";
  }
}
