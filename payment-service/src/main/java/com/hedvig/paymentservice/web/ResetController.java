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
  private static final String TRANSACTION_HISTORY_BACKFILL = "com.hedvig.paymentservice.domain.payments.backfill";
  private static final String MEMBER_RESET_PROCESSOR_NAME = "com.hedvig.paymentservice.query.member";

  private EventProcessingConfiguration eventProcessingConfiguration;

  public ResetController(final EventProcessingConfiguration eventProcessingConfiguration) {
    this.eventProcessingConfiguration = eventProcessingConfiguration;
  }

  @PutMapping("transactionHistory")
  public void backfillTransactionHistory() {
    eventProcessingConfiguration
      .eventProcessor(TRANSACTION_HISTORY_BACKFILL, TrackingEventProcessor.class)
      .ifPresent(trackingEventProcessor -> {
        trackingEventProcessor.shutDown();
        trackingEventProcessor.resetTokens();
        trackingEventProcessor.start();
      });
  }

  @PutMapping("/member")
  public void resetMember() {
    eventProcessingConfiguration
      .eventProcessor(MEMBER_RESET_PROCESSOR_NAME, TrackingEventProcessor.class)
      .ifPresent(trackingEventProcessor -> {
        trackingEventProcessor.shutDown();
        trackingEventProcessor.resetTokens();
        trackingEventProcessor.start();
      });
  }
}
