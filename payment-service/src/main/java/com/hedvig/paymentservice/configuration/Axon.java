package com.hedvig.paymentservice.configuration;

import org.axonframework.config.EventHandlingConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Axon {

  @Autowired
  public void configure(EventHandlingConfiguration config) {
    config.usingTrackingProcessors();
  }
}
