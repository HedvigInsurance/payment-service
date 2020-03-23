package com.hedvig.paymentservice.query.member.entities;

import com.hedvig.paymentservice.domain.payments.DirectDebitStatus;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
public class Member {

  @Id
  public String id;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "member", orphanRemoval = true)
  @MapKey
  Map<UUID, Transaction> transactions = new HashMap<>();

  String trustlyAccountNumber;

  String adyenAccountId;

  @Enumerated(EnumType.STRING)
  DirectDebitStatus directDebitStatus;

  String bank;
  String descriptor;

  public Member() {
  }

  public Transaction getTransaction(UUID transactionId) {
    return this.transactions.get(transactionId);
  }

  public Boolean isDirectDebitMandateActive() {
    return directDebitStatus != null && directDebitStatus.equals(DirectDebitStatus.CONNECTED);
  }

  public String getId() {
    return this.id;
  }

  public Map<UUID, Transaction> getTransactions() {
    return this.transactions;
  }

  public String getTrustlyAccountNumber() {
    return this.trustlyAccountNumber;
  }

  public String getAdyenAccountId() {
    return this.adyenAccountId;
  }

  public DirectDebitStatus getDirectDebitStatus() {
    return this.directDebitStatus;
  }

  public String getBank() {
    return this.bank;
  }

  public String getDescriptor() {
    return this.descriptor;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setTransactions(Map<UUID, Transaction> transactions) {
    this.transactions = transactions;
  }

  public void setTrustlyAccountNumber(String trustlyAccountNumber) {
    this.trustlyAccountNumber = trustlyAccountNumber;
  }

  public void setAdyenAccountId(String adyenAccountId) {
    this.adyenAccountId = adyenAccountId;
  }

  public void setDirectDebitStatus(DirectDebitStatus directDebitStatus) {
    this.directDebitStatus = directDebitStatus;
  }

  public void setBank(String bank) {
    this.bank = bank;
  }

  public void setDescriptor(String descriptor) {
    this.descriptor = descriptor;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof Member)) return false;
    final Member other = (Member) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$id = this.getId();
    final Object other$id = other.getId();
    if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
    final Object this$transactions = this.getTransactions();
    final Object other$transactions = other.getTransactions();
    if (this$transactions == null ? other$transactions != null : !this$transactions.equals(other$transactions))
      return false;
    final Object this$trustlyAccountNumber = this.getTrustlyAccountNumber();
    final Object other$trustlyAccountNumber = other.getTrustlyAccountNumber();
    if (this$trustlyAccountNumber == null ? other$trustlyAccountNumber != null : !this$trustlyAccountNumber.equals(other$trustlyAccountNumber))
      return false;
    final Object this$adyenAccountId = this.getAdyenAccountId();
    final Object other$adyenAccountId = other.getAdyenAccountId();
    if (this$adyenAccountId == null ? other$adyenAccountId != null : !this$adyenAccountId.equals(other$adyenAccountId))
      return false;
    final Object this$directDebitStatus = this.getDirectDebitStatus();
    final Object other$directDebitStatus = other.getDirectDebitStatus();
    if (this$directDebitStatus == null ? other$directDebitStatus != null : !this$directDebitStatus.equals(other$directDebitStatus))
      return false;
    final Object this$bank = this.getBank();
    final Object other$bank = other.getBank();
    if (this$bank == null ? other$bank != null : !this$bank.equals(other$bank)) return false;
    final Object this$descriptor = this.getDescriptor();
    final Object other$descriptor = other.getDescriptor();
    if (this$descriptor == null ? other$descriptor != null : !this$descriptor.equals(other$descriptor))
      return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof Member;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    final Object $transactions = this.getTransactions();
    result = result * PRIME + ($transactions == null ? 43 : $transactions.hashCode());
    final Object $trustlyAccountNumber = this.getTrustlyAccountNumber();
    result = result * PRIME + ($trustlyAccountNumber == null ? 43 : $trustlyAccountNumber.hashCode());
    final Object $adyenAccountId = this.getAdyenAccountId();
    result = result * PRIME + ($adyenAccountId == null ? 43 : $adyenAccountId.hashCode());
    final Object $directDebitStatus = this.getDirectDebitStatus();
    result = result * PRIME + ($directDebitStatus == null ? 43 : $directDebitStatus.hashCode());
    final Object $bank = this.getBank();
    result = result * PRIME + ($bank == null ? 43 : $bank.hashCode());
    final Object $descriptor = this.getDescriptor();
    result = result * PRIME + ($descriptor == null ? 43 : $descriptor.hashCode());
    return result;
  }

  public String toString() {
    return "Member(id=" + this.getId() + ", transactions=" + this.getTransactions() + ", trustlyAccountNumber=" + this.getTrustlyAccountNumber() + ", adyenAccountId=" + this.getAdyenAccountId() + ", directDebitStatus=" + this.getDirectDebitStatus() + ", bank=" + this.getBank() + ", descriptor=" + this.getDescriptor() + ")";
  }
}
