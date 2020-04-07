package com.hedvig.paymentservice.query.member.entities;

import com.hedvig.paymentservice.domain.payments.DirectDebitStatus;
import com.hedvig.paymentservice.graphQl.types.PayinMethodStatus;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Entity
public class Member {

  @Id
  public String id;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "member", orphanRemoval = true)
  @MapKey
  Map<UUID, Transaction> transactions = new HashMap<>();

  String trustlyAccountNumber;

  String adyenRecurringDetailReference;

  @Enumerated(EnumType.STRING)
  DirectDebitStatus directDebitStatus;

  @Enumerated(EnumType.STRING)
  PayinMethodStatus payinMethodStatus;

  String bank;
  String descriptor;

  public Member() {
  }


  public PayinMethodStatus getPayinMethodStatus() {
    return payinMethodStatus;
  }

  public void setPayinMethodStatus(PayinMethodStatus payinMethodStatus) {
    this.payinMethodStatus = payinMethodStatus;
  }

  public Transaction getTransaction(UUID transactionId) {
    return this.transactions.get(transactionId);
  }

  public Boolean isDirectDebitMandateActive() {
    return (directDebitStatus != null && directDebitStatus.equals(DirectDebitStatus.CONNECTED)) || (adyenRecurringDetailReference != null && payinMethodStatus.equals(PayinMethodStatus.ACTIVE));
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

  public String getAdyenRecurringDetailReference() {
    return this.adyenRecurringDetailReference;
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

  public void setAdyenRecurringDetailReference(String adyenRecurringDetailReference) {
    this.adyenRecurringDetailReference = adyenRecurringDetailReference;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Member member = (Member) o;
    return Objects.equals(id, member.id) &&
      Objects.equals(transactions, member.transactions) &&
      Objects.equals(trustlyAccountNumber, member.trustlyAccountNumber) &&
      Objects.equals(adyenRecurringDetailReference, member.adyenRecurringDetailReference) &&
      directDebitStatus == member.directDebitStatus &&
      payinMethodStatus == member.payinMethodStatus &&
      Objects.equals(bank, member.bank) &&
      Objects.equals(descriptor, member.descriptor);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, transactions, trustlyAccountNumber, adyenRecurringDetailReference, directDebitStatus, payinMethodStatus, bank, descriptor);
  }
}
