package com.hedvig.paymentservice.query.registerAccount.enteties;

import com.hedvig.paymentservice.domain.accountRegistration.enums.AccountRegistrationStatus;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.Instant;
import java.util.UUID;

@Entity
public class AccountRegistration {
  @Id
  private UUID accountRegistrationId;
  private String memberId;
  @Enumerated(EnumType.STRING)
  private AccountRegistrationStatus status;
  private UUID hedvigOrderId;
  private String trustlyOrderId;
  private Instant initiated;

  @java.beans.ConstructorProperties({"accountRegistrationId", "memberId", "status", "hedvigOrderId", "trustlyOrderId", "initiated"})
  public AccountRegistration(UUID accountRegistrationId, String memberId, AccountRegistrationStatus status, UUID hedvigOrderId, String trustlyOrderId, Instant initiated) {
    this.accountRegistrationId = accountRegistrationId;
    this.memberId = memberId;
    this.status = status;
    this.hedvigOrderId = hedvigOrderId;
    this.trustlyOrderId = trustlyOrderId;
    this.initiated = initiated;
  }

  public AccountRegistration() {
  }

  public UUID getAccountRegistrationId() {
    return this.accountRegistrationId;
  }

  public String getMemberId() {
    return this.memberId;
  }

  public AccountRegistrationStatus getStatus() {
    return this.status;
  }

  public UUID getHedvigOrderId() {
    return this.hedvigOrderId;
  }

  public String getTrustlyOrderId() {
    return this.trustlyOrderId;
  }

  public Instant getInitiated() {
    return this.initiated;
  }

  public void setAccountRegistrationId(UUID accountRegistrationId) {
    this.accountRegistrationId = accountRegistrationId;
  }

  public void setMemberId(String memberId) {
    this.memberId = memberId;
  }

  public void setStatus(AccountRegistrationStatus status) {
    this.status = status;
  }

  public void setHedvigOrderId(UUID hedvigOrderId) {
    this.hedvigOrderId = hedvigOrderId;
  }

  public void setTrustlyOrderId(String trustlyOrderId) {
    this.trustlyOrderId = trustlyOrderId;
  }

  public void setInitiated(Instant initiated) {
    this.initiated = initiated;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof AccountRegistration)) return false;
    final AccountRegistration other = (AccountRegistration) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$accountRegistrationId = this.getAccountRegistrationId();
    final Object other$accountRegistrationId = other.getAccountRegistrationId();
    if (this$accountRegistrationId == null ? other$accountRegistrationId != null : !this$accountRegistrationId.equals(other$accountRegistrationId))
      return false;
    final Object this$memberId = this.getMemberId();
    final Object other$memberId = other.getMemberId();
    if (this$memberId == null ? other$memberId != null : !this$memberId.equals(other$memberId)) return false;
    final Object this$status = this.getStatus();
    final Object other$status = other.getStatus();
    if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
    final Object this$hedvigOrderId = this.getHedvigOrderId();
    final Object other$hedvigOrderId = other.getHedvigOrderId();
    if (this$hedvigOrderId == null ? other$hedvigOrderId != null : !this$hedvigOrderId.equals(other$hedvigOrderId))
      return false;
    final Object this$trustlyOrderId = this.getTrustlyOrderId();
    final Object other$trustlyOrderId = other.getTrustlyOrderId();
    if (this$trustlyOrderId == null ? other$trustlyOrderId != null : !this$trustlyOrderId.equals(other$trustlyOrderId))
      return false;
    final Object this$initiated = this.getInitiated();
    final Object other$initiated = other.getInitiated();
    if (this$initiated == null ? other$initiated != null : !this$initiated.equals(other$initiated)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof AccountRegistration;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $accountRegistrationId = this.getAccountRegistrationId();
    result = result * PRIME + ($accountRegistrationId == null ? 43 : $accountRegistrationId.hashCode());
    final Object $memberId = this.getMemberId();
    result = result * PRIME + ($memberId == null ? 43 : $memberId.hashCode());
    final Object $status = this.getStatus();
    result = result * PRIME + ($status == null ? 43 : $status.hashCode());
    final Object $hedvigOrderId = this.getHedvigOrderId();
    result = result * PRIME + ($hedvigOrderId == null ? 43 : $hedvigOrderId.hashCode());
    final Object $trustlyOrderId = this.getTrustlyOrderId();
    result = result * PRIME + ($trustlyOrderId == null ? 43 : $trustlyOrderId.hashCode());
    final Object $initiated = this.getInitiated();
    result = result * PRIME + ($initiated == null ? 43 : $initiated.hashCode());
    return result;
  }

  public String toString() {
    return "AccountRegistration(accountRegistrationId=" + this.getAccountRegistrationId() + ", memberId=" + this.getMemberId() + ", status=" + this.getStatus() + ", hedvigOrderId=" + this.getHedvigOrderId() + ", trustlyOrderId=" + this.getTrustlyOrderId() + ", initiated=" + this.getInitiated() + ")";
  }
}
