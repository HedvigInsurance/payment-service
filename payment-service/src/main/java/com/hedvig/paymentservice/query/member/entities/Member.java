package com.hedvig.paymentservice.query.member.entities;

import java.util.*;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Member {
    @Id
    String id;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKey
    Map<UUID, Transaction> transactions = new HashMap<>();

    String trustlyAccountNumber;
    Boolean directDebitMandateActive;


    public Transaction getTransaction(UUID transactionId) {
        return this.transactions.get(transactionId);
    }

}
