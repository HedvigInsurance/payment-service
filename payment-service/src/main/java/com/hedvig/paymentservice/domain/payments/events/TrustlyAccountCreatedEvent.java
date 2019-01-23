package com.hedvig.paymentservice.domain.payments.events;

import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.serialization.Revision;

import java.util.UUID;

@Revision("1.0")
public class TrustlyAccountCreatedEvent {
  @AggregateIdentifier
  String memberId;
  UUID hedvigOrderId;

  String trustlyAccountId;
  String address;
  String bank;
  String city;
  String clearingHouse;
  String descriptor;
  String lastDigits;
  String name;
  String personId;
  String zipCode;

  @java.beans.ConstructorProperties({"memberId", "hedvigOrderId", "trustlyAccountId", "address",
    "bank", "city", "clearingHouse", "descriptor", "lastDigits",
    "name", "personId", "zipCode"})
  public TrustlyAccountCreatedEvent(String memberId, UUID hedvigOrderId, String trustlyAccountId,
                                    String address, String bank, String city, String clearingHouse, String descriptor, String lastDigits, String name, String personId,
                                    String zipCode) {
    this.memberId = memberId;
    this.hedvigOrderId = hedvigOrderId;
    this.trustlyAccountId = trustlyAccountId;
    this.address = address;
    this.bank = bank;
    this.city = city;
    this.clearingHouse = clearingHouse;
    this.descriptor = descriptor;
    this.lastDigits = lastDigits;
    this.name = name;
    this.personId = personId;
    this.zipCode = zipCode;
  }

  public String getMemberId() {
    return this.memberId;
  }

  public UUID getHedvigOrderId() {
    return this.hedvigOrderId;
  }

  public String getTrustlyAccountId() {
    return this.trustlyAccountId;
  }

  public String getAddress() {
    return this.address;
  }

  public String getBank() {
    return this.bank;
  }

  public String getCity() {
    return this.city;
  }

  public String getClearingHouse() {
    return this.clearingHouse;
  }

  public String getDescriptor() {
    return this.descriptor;
  }

  public String getLastDigits() {
    return this.lastDigits;
  }

  public String getName() {
    return this.name;
  }

  public String getPersonId() {
    return this.personId;
  }

  public String getZipCode() {
    return this.zipCode;
  }

  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof TrustlyAccountCreatedEvent)) {
      return false;
    }
    final TrustlyAccountCreatedEvent other = (TrustlyAccountCreatedEvent) o;
    final Object this$memberId = this.getMemberId();
    final Object other$memberId = other.getMemberId();
    if (this$memberId == null ? other$memberId != null : !this$memberId.equals(other$memberId)) {
      return false;
    }
    final Object this$hedvigOrderId = this.getHedvigOrderId();
    final Object other$hedvigOrderId = other.getHedvigOrderId();
    if (this$hedvigOrderId == null ? other$hedvigOrderId != null
      : !this$hedvigOrderId.equals(other$hedvigOrderId)) {
      return false;
    }
    final Object this$trustlyAccountId = this.getTrustlyAccountId();
    final Object other$trustlyAccountId = other.getTrustlyAccountId();
    if (this$trustlyAccountId == null ? other$trustlyAccountId != null
      : !this$trustlyAccountId.equals(other$trustlyAccountId)) {
      return false;
    }
    final Object this$address = this.getAddress();
    final Object other$address = other.getAddress();
    if (this$address == null ? other$address != null : !this$address.equals(other$address)) {
      return false;
    }
    final Object this$bank = this.getBank();
    final Object other$bank = other.getBank();
    if (this$bank == null ? other$bank != null : !this$bank.equals(other$bank)) {
      return false;
    }
    final Object this$city = this.getCity();
    final Object other$city = other.getCity();
    if (this$city == null ? other$city != null : !this$city.equals(other$city)) {
      return false;
    }
    final Object this$clearingHouse = this.getClearingHouse();
    final Object other$clearingHouse = other.getClearingHouse();
    if (this$clearingHouse == null ? other$clearingHouse != null
      : !this$clearingHouse.equals(other$clearingHouse)) {
      return false;
    }
    final Object this$descriptor = this.getDescriptor();
    final Object other$descriptor = other.getDescriptor();
    if (this$descriptor == null ? other$descriptor != null
      : !this$descriptor.equals(other$descriptor)) {
      return false;
    }
    final Object this$lastDigits = this.getLastDigits();
    final Object other$lastDigits = other.getLastDigits();
    if (this$lastDigits == null ? other$lastDigits != null
      : !this$lastDigits.equals(other$lastDigits)) {
      return false;
    }
    final Object this$name = this.getName();
    final Object other$name = other.getName();
    if (this$name == null ? other$name != null : !this$name.equals(other$name)) {
      return false;
    }
    final Object this$personId = this.getPersonId();
    final Object other$personId = other.getPersonId();
    if (this$personId == null ? other$personId != null : !this$personId.equals(other$personId)) {
      return false;
    }
    final Object this$zipCode = this.getZipCode();
    final Object other$zipCode = other.getZipCode();
    if (this$zipCode == null ? other$zipCode != null : !this$zipCode.equals(other$zipCode)) {
      return false;
    }
    return true;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $memberId = this.getMemberId();
    result = result * PRIME + ($memberId == null ? 43 : $memberId.hashCode());
    final Object $hedvigOrderId = this.getHedvigOrderId();
    result = result * PRIME + ($hedvigOrderId == null ? 43 : $hedvigOrderId.hashCode());
    final Object $trustlyAccountId = this.getTrustlyAccountId();
    result = result * PRIME + ($trustlyAccountId == null ? 43 : $trustlyAccountId.hashCode());
    final Object $address = this.getAddress();
    result = result * PRIME + ($address == null ? 43 : $address.hashCode());
    final Object $bank = this.getBank();
    result = result * PRIME + ($bank == null ? 43 : $bank.hashCode());
    final Object $city = this.getCity();
    result = result * PRIME + ($city == null ? 43 : $city.hashCode());
    final Object $clearingHouse = this.getClearingHouse();
    result = result * PRIME + ($clearingHouse == null ? 43 : $clearingHouse.hashCode());
    final Object $descriptor = this.getDescriptor();
    result = result * PRIME + ($descriptor == null ? 43 : $descriptor.hashCode());
    final Object $lastDigits = this.getLastDigits();
    result = result * PRIME + ($lastDigits == null ? 43 : $lastDigits.hashCode());
    final Object $name = this.getName();
    result = result * PRIME + ($name == null ? 43 : $name.hashCode());
    final Object $personId = this.getPersonId();
    result = result * PRIME + ($personId == null ? 43 : $personId.hashCode());
    final Object $zipCode = this.getZipCode();
    result = result * PRIME + ($zipCode == null ? 43 : $zipCode.hashCode());
    return result;
  }

  public String toString() {
    return "TrustlyAccountCreatedEvent(memberId=" + this.getMemberId() + ", hedvigOrderId=" + this
      .getHedvigOrderId() + ", trustlyAccountId=" + this.getTrustlyAccountId() + ", address="
      + this.getAddress() + ", bank=" + this.getBank() + ", city=" + this.getCity()
      + ", clearingHouse=" + this.getClearingHouse() + ", descriptor=" + this.getDescriptor()
      + ", lastDigits="
      + this.getLastDigits() + ", name=" + this.getName() + ", personId=" + this.getPersonId()
      + ", zipCode=" + this.getZipCode() + ")";
  }
}
