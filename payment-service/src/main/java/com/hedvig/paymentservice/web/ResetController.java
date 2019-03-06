package com.hedvig.paymentservice.web;


import com.hedvig.paymentservice.domain.payments.Member;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.TrackingEventProcessor;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping(path = "/reset")
public class ResetController {

  private static String PROCESSOR_NAME = "com.hedvig.paymentservice.query.member";

  private EventProcessingConfiguration eventProcessingConfiguration;

  public ResetController(final EventProcessingConfiguration eventProcessingConfiguration) {
    this.eventProcessingConfiguration = eventProcessingConfiguration;
  }

  @PutMapping("transactionHistory/backfill")
  public void backfillTransactionHistory() {
    // TODO
  }

  @PutMapping("/member")
  public void resetMember() {
    eventProcessingConfiguration
      .eventProcessor(PROCESSOR_NAME, TrackingEventProcessor.class)
      .ifPresent(trackingEventProcessor -> {
        trackingEventProcessor.shutDown();
        trackingEventProcessor.resetTokens();
        trackingEventProcessor.start();
      });
  }
}
