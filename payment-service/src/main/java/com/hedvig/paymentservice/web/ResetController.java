package com.hedvig.paymentservice.web;


import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.TrackingEventProcessor;
import org.axonframework.messaging.StreamableMessageSource;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping(path = "/reset")
public class ResetController {
  private static final String TRANSACTION_HISTORY_PROCESSING_GROUP = "TransactionHistory";
  private static final String MEMBER_RESET_PROCESSOR_NAME = "MemberEventsProcessorGroup";
  private static final String TRUSTLY_SEGMENT_PROCESSOR_GROUP = "TrustlySegmentProcessorGroup";

  private EventProcessingConfiguration eventProcessingConfiguration;

  public ResetController(final EventProcessingConfiguration eventProcessingConfiguration) {
    this.eventProcessingConfiguration = eventProcessingConfiguration;
  }

  @PutMapping("transactionHistory")
  public void backfillTransactionHistory() {
    eventProcessingConfiguration
      .eventProcessorByProcessingGroup(TRANSACTION_HISTORY_PROCESSING_GROUP, TrackingEventProcessor.class)
      .ifPresent(trackingEventProcessor -> {
        trackingEventProcessor.shutDown();
        trackingEventProcessor.resetTokens(StreamableMessageSource::createTailToken);
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

  @PutMapping("trustlySegmentProcessorGroup")
    public void resetTrustlySegmentProcessorGroup(){
      eventProcessingConfiguration
          .eventProcessor(TRUSTLY_SEGMENT_PROCESSOR_GROUP, TrackingEventProcessor.class)
          .ifPresent(trackingEventProcessor -> {
          trackingEventProcessor.shutDown();
          trackingEventProcessor.resetTokens(StreamableMessageSource::createTailToken);
          trackingEventProcessor.start();
      });
  }
}
