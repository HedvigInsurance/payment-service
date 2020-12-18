package com.hedvig.paymentservice.domain.trustlyOrder.commands;

import org.axonframework.commandhandling.TargetAggregateIdentifier;

import javax.money.MonetaryAmount;
import java.time.LocalDate;
import java.util.UUID;

public final class CreatePayoutOrderCommand {
  @TargetAggregateIdentifier
  private final UUID hedvigOrderId;

  private final UUID transactionId;
  private final String memberId;
  private final MonetaryAmount amount;
  private final String trustlyAccountId;
  private final String address;
  private final String countryCode;
  private final LocalDate dateOfBirth;
  private final String firstName;
  private final String lastName;

    @java.beans.ConstructorProperties({"hedvigOrderId", "transactionId", "memberId", "amount", "trustlyAccountId", "address", "countryCode", "dateOfBirth", "firstName", "lastName"})
    public CreatePayoutOrderCommand(UUID hedvigOrderId, UUID transactionId, String memberId, MonetaryAmount amount, String trustlyAccountId, String address, String countryCode, LocalDate dateOfBirth, String firstName, String lastName) {
        this.hedvigOrderId = hedvigOrderId;
        this.transactionId = transactionId;
        this.memberId = memberId;
        this.amount = amount;
        this.trustlyAccountId = trustlyAccountId;
        this.address = address;
        this.countryCode = countryCode;
        this.dateOfBirth = dateOfBirth;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public UUID getHedvigOrderId() {
        return this.hedvigOrderId;
    }

    public UUID getTransactionId() {
        return this.transactionId;
    }

    public String getMemberId() {
        return this.memberId;
    }

    public MonetaryAmount getAmount() {
        return this.amount;
    }

    public String getTrustlyAccountId() {
        return this.trustlyAccountId;
    }

    public String getAddress() {
        return this.address;
    }

    public String getCountryCode() {
        return this.countryCode;
    }

    public LocalDate getDateOfBirth() {
        return this.dateOfBirth;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof CreatePayoutOrderCommand))
            return false;
        final CreatePayoutOrderCommand other = (CreatePayoutOrderCommand) o;
        final Object this$hedvigOrderId = this.getHedvigOrderId();
        final Object other$hedvigOrderId = other.getHedvigOrderId();
        if (this$hedvigOrderId == null ? other$hedvigOrderId != null : !this$hedvigOrderId.equals(other$hedvigOrderId))
            return false;
        final Object this$transactionId = this.getTransactionId();
        final Object other$transactionId = other.getTransactionId();
        if (this$transactionId == null ? other$transactionId != null : !this$transactionId.equals(other$transactionId))
            return false;
        final Object this$memberId = this.getMemberId();
        final Object other$memberId = other.getMemberId();
        if (this$memberId == null ? other$memberId != null : !this$memberId.equals(other$memberId)) return false;
        final Object this$amount = this.getAmount();
        final Object other$amount = other.getAmount();
        if (this$amount == null ? other$amount != null : !this$amount.equals(other$amount)) return false;
        final Object this$trustlyAccountId = this.getTrustlyAccountId();
        final Object other$trustlyAccountId = other.getTrustlyAccountId();
        if (this$trustlyAccountId == null ? other$trustlyAccountId != null : !this$trustlyAccountId.equals(other$trustlyAccountId))
            return false;
        final Object this$address = this.getAddress();
        final Object other$address = other.getAddress();
        if (this$address == null ? other$address != null : !this$address.equals(other$address)) return false;
        final Object this$countryCode = this.getCountryCode();
        final Object other$countryCode = other.getCountryCode();
        if (this$countryCode == null ? other$countryCode != null : !this$countryCode.equals(other$countryCode))
            return false;
        final Object this$dateOfBirth = this.getDateOfBirth();
        final Object other$dateOfBirth = other.getDateOfBirth();
        if (this$dateOfBirth == null ? other$dateOfBirth != null : !this$dateOfBirth.equals(other$dateOfBirth))
            return false;
        final Object this$firstName = this.getFirstName();
        final Object other$firstName = other.getFirstName();
        if (this$firstName == null ? other$firstName != null : !this$firstName.equals(other$firstName)) return false;
        final Object this$lastName = this.getLastName();
        final Object other$lastName = other.getLastName();
        if (this$lastName == null ? other$lastName != null : !this$lastName.equals(other$lastName)) return false;
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $hedvigOrderId = this.getHedvigOrderId();
        result = result * PRIME + ($hedvigOrderId == null ? 43 : $hedvigOrderId.hashCode());
        final Object $transactionId = this.getTransactionId();
        result = result * PRIME + ($transactionId == null ? 43 : $transactionId.hashCode());
        final Object $memberId = this.getMemberId();
        result = result * PRIME + ($memberId == null ? 43 : $memberId.hashCode());
        final Object $amount = this.getAmount();
        result = result * PRIME + ($amount == null ? 43 : $amount.hashCode());
        final Object $trustlyAccountId = this.getTrustlyAccountId();
        result = result * PRIME + ($trustlyAccountId == null ? 43 : $trustlyAccountId.hashCode());
        final Object $address = this.getAddress();
        result = result * PRIME + ($address == null ? 43 : $address.hashCode());
        final Object $countryCode = this.getCountryCode();
        result = result * PRIME + ($countryCode == null ? 43 : $countryCode.hashCode());
        final Object $dateOfBirth = this.getDateOfBirth();
        result = result * PRIME + ($dateOfBirth == null ? 43 : $dateOfBirth.hashCode());
        final Object $firstName = this.getFirstName();
        result = result * PRIME + ($firstName == null ? 43 : $firstName.hashCode());
        final Object $lastName = this.getLastName();
        result = result * PRIME + ($lastName == null ? 43 : $lastName.hashCode());
        return result;
    }

    public String toString() {
        return "CreatePayoutOrderCommand(hedvigOrderId=" + this.getHedvigOrderId() + ", transactionId=" + this.getTransactionId() + ", memberId=" + this.getMemberId() + ", amount=" + this.getAmount() + ", trustlyAccountId=" + this.getTrustlyAccountId() + ", address=" + this.getAddress() + ", countryCode=" + this.getCountryCode() + ", dateOfBirth=" + this.getDateOfBirth() + ", firstName=" + this.getFirstName() + ", lastName=" + this.getLastName() + ")";
    }
}
