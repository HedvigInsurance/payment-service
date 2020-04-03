package com.hedvig.paymentservice.query.adyen.entities;

import com.hedvig.paymentservice.domain.tokenRegistration.enums.TokenRegistrationStatus;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.Instant;
import java.util.UUID;

@Entity
public class AdyenToken {
  @Id
  private UUID adyenTokenId;
  private String memberId;
  @Enumerated(EnumType.STRING)
  private TokenRegistrationStatus tokenStatus;
  private String recurringDetailReference;
  @CreationTimestamp
  private Instant createdAt;

  public AdyenToken() {
  }

  public UUID getAdyenTokenId() {
    return this.adyenTokenId;
  }

  public String getMemberId() {
    return this.memberId;
  }

  public TokenRegistrationStatus getTokenStatus() {
    return this.tokenStatus;
  }

  public String getRecurringDetailReference() {
    return this.recurringDetailReference;
  }

  public Instant getCreatedAt() {
    return this.createdAt;
  }

  public void setAdyenTokenId(UUID adyenTokenId) {
    this.adyenTokenId = adyenTokenId;
  }

  public void setMemberId(String memberId) {
    this.memberId = memberId;
  }

  public void setTokenStatus(TokenRegistrationStatus tokenStatus) {
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
    if (!(o instanceof AdyenToken)) return false;
    final AdyenToken other = (AdyenToken) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$adyenTokenId = this.getAdyenTokenId();
    final Object other$adyenTokenId = other.getAdyenTokenId();
    if (this$adyenTokenId == null ? other$adyenTokenId != null : !this$adyenTokenId.equals(other$adyenTokenId))
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
    return other instanceof AdyenToken;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $adyenTokenId = this.getAdyenTokenId();
    result = result * PRIME + ($adyenTokenId == null ? 43 : $adyenTokenId.hashCode());
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
    return "AdyenToken(adyenTokenId=" + this.getAdyenTokenId() + ", memberId=" + this.getMemberId() + ", tokenStatus=" + this.getTokenStatus() + ", recurringDetailReference=" + this.getRecurringDetailReference() + ", createdAt=" + this.getCreatedAt() + ")";
  }
}
