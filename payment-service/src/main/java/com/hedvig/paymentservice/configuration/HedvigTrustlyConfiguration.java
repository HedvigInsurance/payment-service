package com.hedvig.paymentservice.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties("hedvig.trustly")
public class HedvigTrustlyConfiguration {
  private List<String> validRedirectHosts;

  public HedvigTrustlyConfiguration() {
  }

  public HedvigTrustlyConfiguration(final List<String> validRedirectHosts) {
    this.validRedirectHosts = validRedirectHosts;
  }

  public List<String> getValidRedirectHosts() {
    return validRedirectHosts;
  }

  public HedvigTrustlyConfiguration setValidRedirectHosts(final List<String> validRedirectHosts) {
    this.validRedirectHosts = validRedirectHosts;
    return this;
  }
}
