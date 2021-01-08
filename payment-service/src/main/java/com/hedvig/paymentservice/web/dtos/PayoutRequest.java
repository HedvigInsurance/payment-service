package com.hedvig.paymentservice.web.dtos;

import javax.money.MonetaryAmount;
import java.time.LocalDate;

@Deprecated
public final class PayoutRequest {
  private final MonetaryAmount amount;
  private final String address;
  private final String countryCode;
  private final LocalDate dateOfBirth;
  private final String firstName;
  private final String lastName;

    @java.beans.ConstructorProperties({"amount", "address", "countryCode", "dateOfBirth", "firstName", "lastName"})
    public PayoutRequest(MonetaryAmount amount, String address, String countryCode, LocalDate dateOfBirth, String firstName, String lastName) {
        this.amount = amount;
        this.address = address;
        this.countryCode = countryCode;
        this.dateOfBirth = dateOfBirth;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public MonetaryAmount getAmount() {
        return this.amount;
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
        if (!(o instanceof PayoutRequest)) return false;
        final PayoutRequest other = (PayoutRequest) o;
        final Object this$amount = this.getAmount();
        final Object other$amount = other.getAmount();
        if (this$amount == null ? other$amount != null : !this$amount.equals(other$amount)) return false;
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
        final Object $amount = this.getAmount();
        result = result * PRIME + ($amount == null ? 43 : $amount.hashCode());
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
        return "PayoutRequest(amount=" + this.getAmount() + ", address=" + this.getAddress() + ", countryCode=" + this.getCountryCode() + ", dateOfBirth=" + this.getDateOfBirth() + ", firstName=" + this.getFirstName() + ", lastName=" + this.getLastName() + ")";
    }
}
