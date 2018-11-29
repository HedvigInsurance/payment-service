package com.hedvig.paymentservice.query.member.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;

@Entity
public class Member {
  @Id String id;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @MapKey
  Map<UUID, Transaction> transactions = new HashMap<>();

  String trustlyAccountNumber;
  Boolean directDebitMandateActive;

  public Transaction getTransaction(UUID transactionId) {
    return this.transactions.get(transactionId);
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

  public Boolean getDirectDebitMandateActive() {
    return this.directDebitMandateActive;
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

  public void setDirectDebitMandateActive(Boolean directDebitMandateActive) {
    this.directDebitMandateActive = directDebitMandateActive;
  }
}
