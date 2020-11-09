package com.hedvig.paymentservice.configuration;

import com.hedvig.paymentservice.domain.adyenTokenRegistration.events.upcasters.AdyenTokenRegistrationAuthorisedEventUpcaster;
import com.hedvig.paymentservice.domain.adyenTokenRegistration.events.upcasters.AdyenTokenRegistrationAuthorisedEventUpcasterV2;
import com.hedvig.paymentservice.domain.adyenTokenRegistration.events.upcasters.PendingAdyenTokenRegistrationCreatedEventUpcaster;
import com.hedvig.paymentservice.domain.adyenTokenRegistration.events.upcasters.PendingAdyenTokenRegistrationCreatedEventUpcasterV2;
import com.hedvig.paymentservice.domain.payments.events.upcasters.ChargeCreatedEventUpcaster;
import com.hedvig.paymentservice.domain.payments.events.upcasters.PayoutCreatedEventUpCaster;
import com.hedvig.paymentservice.domain.payments.events.upcasters.TrustlyAccountCreatedUpCaster;
import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.TrackingEventProcessorConfiguration;
import org.axonframework.messaging.StreamableMessageSource;
import org.axonframework.serialization.upcasting.event.EventUpcasterChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Axon {

  @Bean
  public EventUpcasterChain eventUpcasters() {
    return new EventUpcasterChain(
      new TrustlyAccountCreatedUpCaster(),
      new PayoutCreatedEventUpCaster(),
      new ChargeCreatedEventUpcaster(),
      new AdyenTokenRegistrationAuthorisedEventUpcaster(),
      new PendingAdyenTokenRegistrationCreatedEventUpcaster(),
      new AdyenTokenRegistrationAuthorisedEventUpcasterV2(),
      new PendingAdyenTokenRegistrationCreatedEventUpcasterV2()
    );
  }

  @Autowired
  public void configure(EventProcessingConfiguration config) {
    config.usingTrackingProcessors();

    config.registerTrackingEventProcessor("Account", x ->
      TrackingEventProcessorConfiguration
        .forSingleThreadedProcessing()
        .andInitialTrackingToken(StreamableMessageSource::createHeadToken));

    config.registerTrackingEventProcessor("BackfillCharges", x ->
      TrackingEventProcessorConfiguration
        .forSingleThreadedProcessing()
        .andBatchSize(100)
        .andInitialTrackingToken(StreamableMessageSource::createTailToken));

      config.registerTrackingEventProcessor("TrustlySegmentProcessorGroup", x ->
          TrackingEventProcessorConfiguration
              .forSingleThreadedProcessing()
              .andInitialTrackingToken(StreamableMessageSource::createHeadToken));

      config.registerTrackingEventProcessor("AdyenSegmentProcessorGroup", x ->
          TrackingEventProcessorConfiguration
              .forSingleThreadedProcessing()
              .andInitialTrackingToken(StreamableMessageSource::createTailToken));
  }

}
