package com.hedvig.paymentservice.web;


import com.hedvig.paymentservice.query.member.entities.TransactionHistoryEventRepository;
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

  private static String MEMBER_PROCESSOR_NAME = "com.hedvig.paymentservice.query.member";

  private EventProcessingConfiguration eventProcessingConfiguration;
  private final TransactionHistoryEventRepository transactionHistoryEventRepository;

  public ResetController(final EventProcessingConfiguration eventProcessingConfiguration, final TransactionHistoryEventRepository transactionHistoryEventRepository) {
    this.eventProcessingConfiguration = eventProcessingConfiguration;
    this.transactionHistoryEventRepository = transactionHistoryEventRepository;
  }

  @PutMapping("/memberAndTransactionHistoryDangerously")
  public void dangerouslyResetTransactionHistory() {
    eventProcessingConfiguration
      .eventProcessor(MEMBER_PROCESSOR_NAME, TrackingEventProcessor.class)
      .ifPresent(trackingEventProcessor -> {
        trackingEventProcessor.shutDown();
        trackingEventProcessor.resetTokens();
        trackingEventProcessor.start();
      });
  }
}
