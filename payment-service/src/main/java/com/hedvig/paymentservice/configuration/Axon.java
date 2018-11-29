package com.hedvig.paymentservice.configuration;

import org.axonframework.config.EventProcessingConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Axon {

  @Autowired
  public void configure(EventProcessingConfiguration config) {

    config.usingTrackingProcessors();

    config.registerSubscribingEventProcessor("SegmentProcessorGroupLive");
  }

}
