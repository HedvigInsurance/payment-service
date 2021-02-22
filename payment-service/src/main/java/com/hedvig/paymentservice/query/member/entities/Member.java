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

    String adyenMerchantAccount;

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

    public String getAdyenMerchantAccount() {
        return adyenMerchantAccount;
    }

    public void setAdyenMerchantAccount(String adyenMerchantAccount) {
        this.adyenMerchantAccount = adyenMerchantAccount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member)) return false;
        Member member = (Member) o;
        return Objects.equals(getId(), member.getId()) &&
            Objects.equals(getTransactions(), member.getTransactions()) &&
            Objects.equals(getTrustlyAccountNumber(), member.getTrustlyAccountNumber()) &&
            Objects.equals(getAdyenRecurringDetailReference(), member.getAdyenRecurringDetailReference()) &&
            Objects.equals(getAdyenMerchantAccount(), member.getAdyenMerchantAccount()) &&
            getDirectDebitStatus() == member.getDirectDebitStatus() &&
            getPayinMethodStatus() == member.getPayinMethodStatus() &&
            Objects.equals(getBank(), member.getBank()) &&
            Objects.equals(getDescriptor(), member.getDescriptor());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTransactions(), getTrustlyAccountNumber(), getAdyenRecurringDetailReference(), getAdyenMerchantAccount(), getDirectDebitStatus(), getPayinMethodStatus(), getBank(), getDescriptor());
    }
}
