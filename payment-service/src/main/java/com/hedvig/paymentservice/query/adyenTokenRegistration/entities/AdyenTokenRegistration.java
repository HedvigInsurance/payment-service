package com.hedvig.paymentservice.query.adyenTokenRegistration.entities;

import com.hedvig.paymentservice.domain.adyenTokenRegistration.enums.AdyenTokenRegistrationStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.Instant;
import java.util.UUID;

@Entity
public class AdyenTokenRegistration {
  @Id
  private UUID adyenTokenRegistrationId;
  private String memberId;
  @Enumerated(EnumType.STRING)
  private AdyenTokenRegistrationStatus tokenStatus;
  private String recurringDetailReference;
  private String paymentDataFromAction;
  @CreationTimestamp
  Instant createdAt;
  @UpdateTimestamp
  Instant updatedAt;

  public AdyenTokenRegistration() {
  }

  public UUID getAdyenTokenRegistrationId() {
    return this.adyenTokenRegistrationId;
  }

  public String getMemberId() {
    return this.memberId;
  }

  public AdyenTokenRegistrationStatus getTokenStatus() {
    return this.tokenStatus;
  }

  public String getRecurringDetailReference() {
    return this.recurringDetailReference;
  }

  public String getPaymentDataFromAction() {
    return this.paymentDataFromAction;
  }

  public Instant getCreatedAt() {
    return this.createdAt;
  }

  public Instant getUpdatedAt() {
    return this.updatedAt;
  }

  public void setAdyenTokenRegistrationId(UUID adyenTokenRegistrationId) {
    this.adyenTokenRegistrationId = adyenTokenRegistrationId;
  }

  public void setMemberId(String memberId) {
    this.memberId = memberId;
  }

  public void setTokenStatus(AdyenTokenRegistrationStatus tokenStatus) {
    this.tokenStatus = tokenStatus;
  }

  public void setRecurringDetailReference(String recurringDetailReference) {
    this.recurringDetailReference = recurringDetailReference;
  }

  public void setPaymentDataFromAction(String paymentDataFromAction) {
    this.paymentDataFromAction = paymentDataFromAction;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof AdyenTokenRegistration))
      return false;
    final AdyenTokenRegistration other = (AdyenTokenRegistration) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$adyenTokenRegistrationId = this.getAdyenTokenRegistrationId();
    final Object other$adyenTokenRegistrationId = other.getAdyenTokenRegistrationId();
    if (this$adyenTokenRegistrationId == null ? other$adyenTokenRegistrationId != null : !this$adyenTokenRegistrationId.equals(other$adyenTokenRegistrationId))
      return false;
    final Object this$memberId = this.getMemberId();
    final Object other$memberId = other.getMemberId();
    if (this$memberId == null ? other$memberId != null : !this$memberId.equals(other$memberId)) return false;
    final Object this$tokenStatus = this.getTokenStatus();
    final Object other$tokenStatus = other.getTokenStatus();
    if (this$tokenStatus == null ? other$tokenStatus != null : !this$tokenStatus.equals(other$tokenStatus))
      return false;
    final Object this$recurringDetailReference = this.getRecurringDetailReference();
    final Object other$recurringDetailReference = other.getRecurringDetailReference();
    if (this$recurringDetailReference == null ? other$recurringDetailReference != null : !this$recurringDetailReference.equals(other$recurringDetailReference))
      return false;
    final Object this$paymentDataFromAction = this.getPaymentDataFromAction();
    final Object other$paymentDataFromAction = other.getPaymentDataFromAction();
    if (this$paymentDataFromAction == null ? other$paymentDataFromAction != null : !this$paymentDataFromAction.equals(other$paymentDataFromAction))
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
    return other instanceof AdyenTokenRegistration;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $adyenTokenRegistrationId = this.getAdyenTokenRegistrationId();
    result = result * PRIME + ($adyenTokenRegistrationId == null ? 43 : $adyenTokenRegistrationId.hashCode());
    final Object $memberId = this.getMemberId();
    result = result * PRIME + ($memberId == null ? 43 : $memberId.hashCode());
    final Object $tokenStatus = this.getTokenStatus();
    result = result * PRIME + ($tokenStatus == null ? 43 : $tokenStatus.hashCode());
    final Object $recurringDetailReference = this.getRecurringDetailReference();
    result = result * PRIME + ($recurringDetailReference == null ? 43 : $recurringDetailReference.hashCode());
    final Object $paymentDataFromAction = this.getPaymentDataFromAction();
    result = result * PRIME + ($paymentDataFromAction == null ? 43 : $paymentDataFromAction.hashCode());
    final Object $createdAt = this.getCreatedAt();
    result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
    final Object $updatedAt = this.getUpdatedAt();
    result = result * PRIME + ($updatedAt == null ? 43 : $updatedAt.hashCode());
    return result;
  }

  public String toString() {
    return "AdyenTokenRegistration(adyenTokenRegistrationId=" + this.getAdyenTokenRegistrationId() + ", memberId=" + this.getMemberId() + ", tokenStatus=" + this.getTokenStatus() + ", recurringDetailReference=" + this.getRecurringDetailReference() + ", paymentDataFromAction=" + this.getPaymentDataFromAction() + ", createdAt=" + this.getCreatedAt() + ", updatedAt=" + this.getUpdatedAt() + ")";
  }
}
