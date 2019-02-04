package com.hedvig.paymentservice.configuration;

import com.hedvig.paymentservice.domain.payments.events.upcasters.TrustlyAccountCreatedUpCaster;
import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.serialization.upcasting.event.EventUpcasterChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Axon {

  @Bean
  public EventUpcasterChain eventUpcasters() {
    return new EventUpcasterChain(new TrustlyAccountCreatedUpCaster());
  }

  @Autowired
  public void configure(EventProcessingConfiguration config) {

    config.usingTrackingProcessors();

    config.registerSubscribingEventProcessor("SegmentProcessorGroupLive");
  }

}
