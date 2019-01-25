package com.hedvig.paymentservice.web;


import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.TrackingEventProcessor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping(path = "/reset")
public class ResetController {

  private static String MEMBER_EVENT_NAME = "com.hedvig.paymentservice.query.member";

  private EventProcessingConfiguration eventProcessingConfiguration;

  public ResetController(EventProcessingConfiguration eventProcessingConfiguration) {
    this.eventProcessingConfiguration = eventProcessingConfiguration;
  }

  @PutMapping("/member")
  public void resetMember() {
    eventProcessingConfiguration
      .eventProcessor(MEMBER_EVENT_NAME, TrackingEventProcessor.class)
      .ifPresent(trackingEventProcessor -> {
        trackingEventProcessor.shutDown();
        trackingEventProcessor.resetTokens();
        trackingEventProcessor.start();
      });
  }
}
