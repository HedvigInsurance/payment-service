package com.hedvig.paymentService.trustly.data.request.requestdata;

import com.google.gson.annotations.SerializedName;
import com.hedvig.paymentService.trustly.commons.Currency;
import com.hedvig.paymentService.trustly.data.request.RequestData;

public class AccountPayoutData extends RequestData {
  @SerializedName("AccountID")
  private String accountID;

  @SerializedName("NotificationURL")
  private String notificationURL;

  @SerializedName("EndUserID")
  private String endUserID;

  @SerializedName("MessageID")
  private String messageID;

  @SerializedName("Amount")
  private String amount;

  @SerializedName("Currency")
  private Currency currency;

  public String getAccountID() {
    return accountID;
  }

  public void setAccountID(String accountID) {
    this.accountID = accountID;
  }

  public String getNotificationURL() {
    return notificationURL;
  }

  public void setNotificationURL(String notificationURL) {
    this.notificationURL = notificationURL;
  }

  public String getEndUserID() {
    return endUserID;
  }

  public void setEndUserID(String endUserID) {
    this.endUserID = endUserID;
  }

  public String getMessageID() {
    return messageID;
  }

  public void setMessageID(String messageID) {
    this.messageID = messageID;
  }

  public String getAmount() {
    return amount;
  }

  public void setAmount(String amount) {
    this.amount = amount;
  }

  public Currency getCurrency() {
    return currency;
  }

  public void setCurrency(Currency currency) {
    this.currency = currency;
  }
}
