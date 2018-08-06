package com.hedvig.paymentservice.services.trustly;

import java.util.Map;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value
public class AccountNotification {
  private static final Logger log = LoggerFactory.getLogger(AccountNotification.class);
  String accountId;
  String address;
  String bank;
  String city;
  String clearingHouse;
  String descriptor;
  boolean directDebitMandate;
  String lastDigits;
  String name;
  String personId;
  String zipCode;

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
}
