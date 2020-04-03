package com.hedvig.paymentservice.query.adyenTokenRegistration.entities;

import com.hedvig.paymentservice.domain.adyenTokenRegistration.enums.AdyenTokenRegistrationStatus;
import org.hibernate.annotations.CreationTimestamp;

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
  @CreationTimestamp
  private Instant createdAt;

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

  public Instant getCreatedAt() {
    return this.createdAt;
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

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof AdyenTokenRegistration))
      return false;
    final AdyenTokenRegistration other = (AdyenTokenRegistration) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$tokenRegistrationId = this.getAdyenTokenRegistrationId();
    final Object other$tokenRegistrationId = other.getAdyenTokenRegistrationId();
    if (this$tokenRegistrationId == null ? other$tokenRegistrationId != null : !this$tokenRegistrationId.equals(other$tokenRegistrationId))
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
    final Object this$createdAt = this.getCreatedAt();
    final Object other$createdAt = other.getCreatedAt();
    if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof AdyenTokenRegistration;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $tokenRegistrationId = this.getAdyenTokenRegistrationId();
    result = result * PRIME + ($tokenRegistrationId == null ? 43 : $tokenRegistrationId.hashCode());
    final Object $memberId = this.getMemberId();
    result = result * PRIME + ($memberId == null ? 43 : $memberId.hashCode());
    final Object $tokenStatus = this.getTokenStatus();
    result = result * PRIME + ($tokenStatus == null ? 43 : $tokenStatus.hashCode());
    final Object $recurringDetailReference = this.getRecurringDetailReference();
    result = result * PRIME + ($recurringDetailReference == null ? 43 : $recurringDetailReference.hashCode());
    final Object $createdAt = this.getCreatedAt();
    result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
    return result;
  }

  public String toString() {
    return "AdyenTokenRegistration(tokenRegistrationId=" + this.getAdyenTokenRegistrationId() + ", memberId=" + this.getMemberId() + ", tokenStatus=" + this.getTokenStatus() + ", recurringDetailReference=" + this.getRecurringDetailReference() + ", createdAt=" + this.getCreatedAt() + ")";
  }
}
