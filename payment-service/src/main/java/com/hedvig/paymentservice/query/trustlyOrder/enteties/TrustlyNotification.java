package com.hedvig.paymentservice.query.trustlyOrder.enteties;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class TrustlyNotification {

  @Id String notificationId;

  @ManyToOne
  @JoinColumn(name = "order_id", nullable = false)
  TrustlyOrder order;

  String accountId;
  String address;
  String bank;
  String city;
  String clearingHouse;
  String descriptor;
  Boolean directDebitMandate;
  String lastDigits;
  String name;
  String personId;
  String zipCode;

    public TrustlyNotification() {
    }

    public String getNotificationId() {
        return this.notificationId;
    }

    public TrustlyOrder getOrder() {
        return this.order;
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

    public Boolean getDirectDebitMandate() {
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

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public void setOrder(TrustlyOrder order) {
        this.order = order;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setClearingHouse(String clearingHouse) {
        this.clearingHouse = clearingHouse;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    public void setDirectDebitMandate(Boolean directDebitMandate) {
        this.directDebitMandate = directDebitMandate;
    }

    public void setLastDigits(String lastDigits) {
        this.lastDigits = lastDigits;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String toString() {
        return "TrustlyNotification(notificationId=" + this.getNotificationId() + ", order=" + this.getOrder() + ", accountId=" + this.getAccountId() + ", address=" + this.getAddress() + ", bank=" + this.getBank() + ", city=" + this.getCity() + ", clearingHouse=" + this.getClearingHouse() + ", descriptor=" + this.getDescriptor() + ", directDebitMandate=" + this.getDirectDebitMandate() + ", lastDigits=" + this.getLastDigits() + ", name=" + this.getName() + ", personId=" + this.getPersonId() + ", zipCode=" + this.getZipCode() + ")";
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof TrustlyNotification)) return false;
        final TrustlyNotification other = (TrustlyNotification) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$notificationId = this.getNotificationId();
        final Object other$notificationId = other.getNotificationId();
        if (this$notificationId == null ? other$notificationId != null : !this$notificationId.equals(other$notificationId))
            return false;
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
        final Object this$directDebitMandate = this.getDirectDebitMandate();
        final Object other$directDebitMandate = other.getDirectDebitMandate();
        if (this$directDebitMandate == null ? other$directDebitMandate != null : !this$directDebitMandate.equals(other$directDebitMandate))
            return false;
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

    protected boolean canEqual(final Object other) {
        return other instanceof TrustlyNotification;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $notificationId = this.getNotificationId();
        result = result * PRIME + ($notificationId == null ? 43 : $notificationId.hashCode());
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
        final Object $directDebitMandate = this.getDirectDebitMandate();
        result = result * PRIME + ($directDebitMandate == null ? 43 : $directDebitMandate.hashCode());
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
}
