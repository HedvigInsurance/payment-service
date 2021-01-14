package com.hedvig.paymentservice.query.adyenTokenRegistration.entities;

import com.hedvig.paymentservice.domain.adyenTokenRegistration.enums.AdyenTokenRegistrationStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
public class AdyenTokenRegistration {
    @Id
    private UUID adyenTokenRegistrationId;
    private String memberId;
    @Enumerated(EnumType.STRING)
    private AdyenTokenRegistrationStatus tokenStatus;
    private String recurringDetailReference;
    @Column(columnDefinition = "TEXT")
    private String paymentDataFromAction;
    @CreationTimestamp
    Instant createdAt;
    @UpdateTimestamp
    Instant updatedAt;
    @Column(columnDefinition = "boolean default false")
    private boolean isForPayout;
    private String shopperReference;

    public AdyenTokenRegistration() {
    }

    public UUID getAdyenTokenRegistrationId() {
        return this.adyenTokenRegistrationId;
    }

    public String getMemberId() {
        return this.memberId;
    }

    public AdyenTokenRegistrationStatus getTokenStatus() {
        return this.tokenStatus;
    }

    public String getRecurringDetailReference() {
        return this.recurringDetailReference;
    }

    public String getPaymentDataFromAction() {
        return this.paymentDataFromAction;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    public void setAdyenTokenRegistrationId(UUID adyenTokenRegistrationId) {
        this.adyenTokenRegistrationId = adyenTokenRegistrationId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public void setTokenStatus(AdyenTokenRegistrationStatus tokenStatus) {
        this.tokenStatus = tokenStatus;
    }

    public void setRecurringDetailReference(String recurringDetailReference) {
        this.recurringDetailReference = recurringDetailReference;
    }

    public void setPaymentDataFromAction(String paymentDataFromAction) {
        this.paymentDataFromAction = paymentDataFromAction;
    }

    public void setIsForPayout(boolean isForPayout) {
        this.isForPayout = isForPayout;
    }

    public boolean getIsForPayout() {
        return isForPayout;
    }

    public void setShopperReference(String shopperReference) {
        this.shopperReference = shopperReference;
    }

    public String getShopperReference() {
        return shopperReference;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AdyenTokenRegistration)) return false;
        AdyenTokenRegistration that = (AdyenTokenRegistration) o;
        return isForPayout == that.isForPayout &&
            Objects.equals(getAdyenTokenRegistrationId(), that.getAdyenTokenRegistrationId()) &&
            Objects.equals(getMemberId(), that.getMemberId()) && getTokenStatus() == that.getTokenStatus() &&
            Objects.equals(getRecurringDetailReference(), that.getRecurringDetailReference()) &&
            Objects.equals(getPaymentDataFromAction(), that.getPaymentDataFromAction()) &&
            Objects.equals(getCreatedAt(), that.getCreatedAt()) &&
            Objects.equals(getUpdatedAt(), that.getUpdatedAt()) &&
            Objects.equals(getShopperReference(), that.getShopperReference());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAdyenTokenRegistrationId(),
            getMemberId(),
            getTokenStatus(),
            getRecurringDetailReference(),
            getPaymentDataFromAction(),
            getCreatedAt(),
            getUpdatedAt(),
            isForPayout,
            getShopperReference());
    }
}
