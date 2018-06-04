package com.hedvig.paymentservice.domain.payments.events;

import lombok.Value;

import java.util.UUID;

@Value
public class ChargeFailedEvent {
    String memberId;
    UUID transactionId;
}
