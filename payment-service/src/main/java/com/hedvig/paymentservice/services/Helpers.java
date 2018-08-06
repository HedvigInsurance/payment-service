package com.hedvig.paymentservice.services;

public class Helpers {

  public static String createTrustlyInboxfromMemberId(String memberId) {
    return String.format("trustly-customer-inbox+%s@hedvig.com", memberId);
  }
}
