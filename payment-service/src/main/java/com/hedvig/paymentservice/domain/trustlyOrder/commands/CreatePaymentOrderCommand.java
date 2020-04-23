package com.hedvig.paymentservice.domain.trustlyOrder.commands;

import org.axonframework.commandhandling.TargetAggregateIdentifier;

import javax.money.MonetaryAmount;
import java.util.UUID;

public final class CreatePaymentOrderCommand {
  @TargetAggregateIdentifier
  private final UUID hedvigOrderId;

  private final UUID transactionId;
  private final String memberId;
  private final MonetaryAmount amount;
  private final String accountId;

  @java.beans.ConstructorProperties({"hedvigOrderId", "transactionId", "memberId", "amount", "accountId"})
  public CreatePaymentOrderCommand(UUID hedvigOrderId, UUID transactionId, String memberId, MonetaryAmount amount, String accountId) {
    this.hedvigOrderId = hedvigOrderId;
    this.transactionId = transactionId;
    this.memberId = memberId;
    this.amount = amount;
    this.accountId = accountId;
  }

  public UUID getHedvigOrderId() {
    return this.hedvigOrderId;
  }

  public UUID getTransactionId() {
    return this.transactionId;
  }

  public String getMemberId() {
    return this.memberId;
  }

  public MonetaryAmount getAmount() {
    return this.amount;
  }

  public String getAccountId() {
    return this.accountId;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof CreatePaymentOrderCommand))
      return false;
    final CreatePaymentOrderCommand other = (CreatePaymentOrderCommand) o;
    final Object this$hedvigOrderId = this.getHedvigOrderId();
    final Object other$hedvigOrderId = other.getHedvigOrderId();
    if (this$hedvigOrderId == null ? other$hedvigOrderId != null : !this$hedvigOrderId.equals(other$hedvigOrderId))
      return false;
    final Object this$transactionId = this.getTransactionId();
    final Object other$transactionId = other.getTransactionId();
    if (this$transactionId == null ? other$transactionId != null : !this$transactionId.equals(other$transactionId))
      return false;
    final Object this$memberId = this.getMemberId();
    final Object other$memberId = other.getMemberId();
    if (this$memberId == null ? other$memberId != null : !this$memberId.equals(other$memberId)) return false;
    final Object this$amount = this.getAmount();
    final Object other$amount = other.getAmount();
    if (this$amount == null ? other$amount != null : !this$amount.equals(other$amount)) return false;
    final Object this$accountId = this.getAccountId();
    final Object other$accountId = other.getAccountId();
    if (this$accountId == null ? other$accountId != null : !this$accountId.equals(other$accountId)) return false;
    return true;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $hedvigOrderId = this.getHedvigOrderId();
    result = result * PRIME + ($hedvigOrderId == null ? 43 : $hedvigOrderId.hashCode());
    final Object $transactionId = this.getTransactionId();
    result = result * PRIME + ($transactionId == null ? 43 : $transactionId.hashCode());
    final Object $memberId = this.getMemberId();
    result = result * PRIME + ($memberId == null ? 43 : $memberId.hashCode());
    final Object $amount = this.getAmount();
    result = result * PRIME + ($amount == null ? 43 : $amount.hashCode());
    final Object $accountId = this.getAccountId();
    result = result * PRIME + ($accountId == null ? 43 : $accountId.hashCode());
    return result;
  }

  public String toString() {
    return "CreatePaymentOrderCommand(hedvigOrderId=" + this.getHedvigOrderId() + ", transactionId=" + this.getTransactionId() + ", memberId=" + this.getMemberId() + ", amount=" + this.getAmount() + ", accountId=" + this.getAccountId() + ")";
  }
}
