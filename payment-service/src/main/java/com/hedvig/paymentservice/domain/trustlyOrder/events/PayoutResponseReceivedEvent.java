package com.hedvig.paymentservice.domain.trustlyOrder.events;

import java.util.UUID;

import lombok.Value;

@Value
public class PayoutResponseReceivedEvent {
    UUID hedvigOrderId;

    String memberId;
    UUID transactionId;
}
