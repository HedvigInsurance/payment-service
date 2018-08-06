package com.hedvig.paymentService.trustly.requestbuilders;

import com.hedvig.paymentService.trustly.commons.Currency;
import com.hedvig.paymentService.trustly.commons.Method;
import com.hedvig.paymentService.trustly.data.request.Request;
import com.hedvig.paymentService.trustly.data.request.RequestParameters;
import com.hedvig.paymentService.trustly.data.request.requestdata.AccountPayoutData;
import com.hedvig.paymentService.trustly.security.SignatureHandler;
import java.util.Map;
import java.util.TreeMap;

public class AccountPayout {
  private final Request request = new Request();

  private AccountPayout(final Build builder) {
    final RequestParameters params = new RequestParameters();
    params.setUUID(SignatureHandler.generateNewUUID());
    params.setData(builder.data);
    request.setMethod(Method.ACCOUNT_PAYOUT);
    request.setParams(params);
  }

  public Request getRequest() {
    return request;
  }

  public static class Build {
    private final AccountPayoutData data = new AccountPayoutData();
    private final Map<String, Object> attributes = new TreeMap();

    public Build(
        final String accountID,
        final String notificationURL,
        final String endUserID,
        final String messageID,
        final String amount,
        final Currency currency,
        final String address,
        final String countryCode,
        final String dateOfBirth,
        final String firstName,
        final String lastName,
        final String partyType,
        final String shopperStatement) {
      data.setAccountID(accountID);
      data.setNotificationURL(notificationURL);
      data.setEndUserID(endUserID);
      data.setMessageID(messageID);
      data.setAmount(amount);
      data.setCurrency(currency);

      attributes.put("Address", address);
      attributes.put("CountryCode", countryCode);
      attributes.put("DateOfBirth", dateOfBirth);
      attributes.put("Firstname", firstName);
      attributes.put("Lastname", lastName);
      attributes.put("PartyType", partyType);
      attributes.put("ShopperStatement", shopperStatement);
    }

    public Request getRequest() {
      return new AccountPayout(this).getRequest();
    }
  }
}
