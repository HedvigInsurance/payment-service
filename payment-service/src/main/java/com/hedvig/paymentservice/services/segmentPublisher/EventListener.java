package com.hedvig.paymentservice.services.segmentPublisher;

import com.google.common.collect.ImmutableMap;
import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountCreatedEvent;
import com.segment.analytics.Analytics;
import com.segment.analytics.messages.IdentifyMessage;
import java.util.Map;
import lombok.val;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
public class EventListener {

    private final Analytics segmentAnalytics;

    public EventListener(Analytics analytics) {
        this.segmentAnalytics = analytics;
    }

    @EventHandler
    public void on(TrustlyAccountCreatedEvent evt) {


        ImmutableMap<String, Object> traits = ImmutableMap.of("is_direct_debit_activated", evt.isDirectDebitMandateActivated());

        segmentIdentify(traits, evt.getMemberId());

    }

    private void segmentIdentify(Map<String, Object> traitsMap, String memberId) {
        segmentAnalytics.enqueue(
            IdentifyMessage.builder()
                .userId(memberId)
                .enableIntegration("All", false)
                .enableIntegration("Customer.io", true)
                .traits(traitsMap));
    }

}
