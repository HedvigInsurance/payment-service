package com.hedvig.paymentservice.services.trustly;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public final class AccountNotification {
  private static final Logger log = LoggerFactory.getLogger(AccountNotification.class);
  private final String accountId;
  private final String address;
  private final String bank;
  private final String city;
  private final String clearingHouse;
  private final String descriptor;
  private final boolean directDebitMandate;
  private final String lastDigits;
  private final String name;
  private final String personId;
  private final String zipCode;

    @java.beans.ConstructorProperties({"accountId", "address", "bank", "city", "clearingHouse", "descriptor", "directDebitMandate", "lastDigits", "name", "personId", "zipCode"})
    public AccountNotification(String accountId, String address, String bank, String city, String clearingHouse, String descriptor, boolean directDebitMandate, String lastDigits, String name, String personId, String zipCode) {
        this.accountId = accountId;
        this.address = address;
        this.bank = bank;
        this.city = city;
        this.clearingHouse = clearingHouse;
        this.descriptor = descriptor;
        this.directDebitMandate = directDebitMandate;
        this.lastDigits = lastDigits;
        this.name = name;
        this.personId = personId;
        this.zipCode = zipCode;
    }

    public static AccountNotification construct(String accountId, Map<String, Object> attributes) {

    String directDebitMandateString = getValueAsString(attributes, "directdebitmandate");
    boolean directDebitMandate =
        directDebitMandateString != null && directDebitMandateString.equals("1");

    String lastDigits = getValueAsString(attributes, "lastdigits");
    String clearingHouse = getValueAsString(attributes, "clearinghouse");
    String bank = getValueAsString(attributes, "bank");
    String descriptor = getValueAsString(attributes, "descriptor");
    String personId = getValueAsString(attributes, "personid");
    String name = getValueAsString(attributes, "name");
    String address = getValueAsString(attributes, "address");
    String zipCode = getValueAsString(attributes, "zipcode");
    String city = getValueAsString(attributes, "city");

    return new AccountNotification(
        accountId,
        address,
        bank,
        city,
        clearingHouse,
        descriptor,
        directDebitMandate,
        lastDigits,
        name,
        personId,
        zipCode);
  }

  public static String getValueAsString(Map<String, Object> attributes, String lastdigits) {
    try {
      return (String) attributes.get(lastdigits);
    } catch (ClassCastException ex) {
      log.error(
          "Could not cast field : {}, from AccountNotification to string: {}", lastdigits, ex);
      return null;
    }
  }

    public String getAccountId() {
        return this.accountId;
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

    public boolean isDirectDebitMandate() {
        return this.directDebitMandate;
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

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof AccountNotification)) return false;
        final AccountNotification other = (AccountNotification) o;
        final Object this$accountId = this.getAccountId();
        final Object other$accountId = other.getAccountId();
        if (this$accountId == null ? other$accountId != null : !this$accountId.equals(other$accountId)) return false;
        final Object this$address = this.getAddress();
        final Object other$address = other.getAddress();
        if (this$address == null ? other$address != null : !this$address.equals(other$address)) return false;
        final Object this$bank = this.getBank();
        final Object other$bank = other.getBank();
        if (this$bank == null ? other$bank != null : !this$bank.equals(other$bank)) return false;
        final Object this$city = this.getCity();
        final Object other$city = other.getCity();
        if (this$city == null ? other$city != null : !this$city.equals(other$city)) return false;
        final Object this$clearingHouse = this.getClearingHouse();
        final Object other$clearingHouse = other.getClearingHouse();
        if (this$clearingHouse == null ? other$clearingHouse != null : !this$clearingHouse.equals(other$clearingHouse))
            return false;
        final Object this$descriptor = this.getDescriptor();
        final Object other$descriptor = other.getDescriptor();
        if (this$descriptor == null ? other$descriptor != null : !this$descriptor.equals(other$descriptor))
            return false;
        if (this.isDirectDebitMandate() != other.isDirectDebitMandate()) return false;
        final Object this$lastDigits = this.getLastDigits();
        final Object other$lastDigits = other.getLastDigits();
        if (this$lastDigits == null ? other$lastDigits != null : !this$lastDigits.equals(other$lastDigits))
            return false;
        final Object this$name = this.getName();
        final Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        final Object this$personId = this.getPersonId();
        final Object other$personId = other.getPersonId();
        if (this$personId == null ? other$personId != null : !this$personId.equals(other$personId)) return false;
        final Object this$zipCode = this.getZipCode();
        final Object other$zipCode = other.getZipCode();
        if (this$zipCode == null ? other$zipCode != null : !this$zipCode.equals(other$zipCode)) return false;
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $accountId = this.getAccountId();
        result = result * PRIME + ($accountId == null ? 43 : $accountId.hashCode());
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
        result = result * PRIME + (this.isDirectDebitMandate() ? 79 : 97);
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
        return "AccountNotification(accountId=" + this.getAccountId() + ", address=" + this.getAddress() + ", bank=" + this.getBank() + ", city=" + this.getCity() + ", clearingHouse=" + this.getClearingHouse() + ", descriptor=" + this.getDescriptor() + ", directDebitMandate=" + this.isDirectDebitMandate() + ", lastDigits=" + this.getLastDigits() + ", name=" + this.getName() + ", personId=" + this.getPersonId() + ", zipCode=" + this.getZipCode() + ")";
    }
}
