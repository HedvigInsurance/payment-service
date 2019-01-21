package com.hedvig.paymentservice.query.member.entities;

import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Data
public class Member {

  @Id
  public String id;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @MapKey
  Map<UUID, Transaction> transactions = new HashMap<>();

  String trustlyAccountNumber;
  boolean directDebitMandateActive;

  String bank;
  String lastDigits;

  public Transaction getTransaction(UUID transactionId) {
    return this.transactions.get(transactionId);
  }

}
