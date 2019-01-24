package com.hedvig.paymentservice.query.member.entities;

import com.hedvig.paymentservice.domain.payments.DirectDebitStatus;
import lombok.Data;

import javax.persistence.*;
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

  @Enumerated(EnumType.STRING)
  DirectDebitStatus directDebitStatus;

  String bank;
  String descriptor;

  public Transaction getTransaction(UUID transactionId) {
    return this.transactions.get(transactionId);
  }

  public Boolean isDirectDebitMandateActive() {
    return directDebitStatus != null && directDebitStatus.equals(DirectDebitStatus.CONNECTED);
  }
}
