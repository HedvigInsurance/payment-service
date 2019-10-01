package com.hedvig.paymentservice.graphQl.types;

import graphql.schema.GraphQLInputType;

public class RegisterDirectDebitClientContextInput implements GraphQLInputType {
  private String successUrl;
  private String failUrl;

  public RegisterDirectDebitClientContextInput() {
  }

  public RegisterDirectDebitClientContextInput(final String successUrl, final String failUrl) {
    this.successUrl = successUrl;
    this.failUrl = failUrl;
  }

  @Override
  public String getName() {
    return "RegisterDirectDebitClientContextInput";
  }

  public String getSuccessUrl() {
    return successUrl;
  }

  public String getFailUrl() {
    return failUrl;
  }
}
