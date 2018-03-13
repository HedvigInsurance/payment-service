package com.hedvig.paymentservice.query.member.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Member {
    @Id
    String id;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    List<Transaction> transactions = new ArrayList<Transaction>();

    String trustlyAccountNumber;
    Boolean directDebitMandateActive;
}
