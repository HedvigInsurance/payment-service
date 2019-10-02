package com.hedvig.paymentservice.graphQl.types;

public class RegisterDirectDebitClientContext {
  private String successUrl;
  private String failureUrl;

  public RegisterDirectDebitClientContext() {
  }

  public RegisterDirectDebitClientContext(final String successUrl, final String failureUrl) {
    this.successUrl = successUrl;
    this.failureUrl = failureUrl;
  }

  public String getSuccessUrl() {
    return successUrl;
  }

  public String getFailureUrl() {
    return failureUrl;
  }
}
