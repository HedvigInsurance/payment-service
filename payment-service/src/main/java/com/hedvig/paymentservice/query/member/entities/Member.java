package com.hedvig.paymentservice.query.member.entities;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
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

    public Member() {
    }

    public Transaction getTransaction(UUID transactionId) {
        return this.transactions.get(transactionId);
    }

    public String getId() {
        return this.id;
    }

    public Map<UUID, Transaction> getTransactions() {
        return this.transactions;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTransactions(Map<UUID, Transaction> transactions) {
        this.transactions = transactions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member)) return false;
        Member member = (Member) o;
        return Objects.equals(getId(), member.getId()) &&
            Objects.equals(getTransactions(), member.getTransactions());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTransactions());
    }
}
