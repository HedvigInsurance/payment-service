package com.hedvig.paymentservice.web;


import com.hedvig.paymentservice.query.member.entities.MemberRepository;
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

  private MemberRepository memberRepository;

  private EventProcessingConfiguration eventProcessingConfiguration;

  public ResetController(MemberRepository repository, EventProcessingConfiguration eventProcessingConfiguration) {
    this.memberRepository = repository;
    this.eventProcessingConfiguration = eventProcessingConfiguration;
  }

  @PutMapping("/member")
  public void resetMember() {
    eventProcessingConfiguration.eventProcessorByProcessingGroup("member-projection", TrackingEventProcessor.class)
      .ifPresent(trackingEventProcessor -> {
        trackingEventProcessor.shutDown();
        trackingEventProcessor.resetTokens();
        trackingEventProcessor.start();
      });
  }
}
