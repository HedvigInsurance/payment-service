package com.hedvig.paymentservice.domain.trustlyOrder.events;

import java.util.UUID;

import com.hedvig.paymentService.trustly.data.response.Error;

import lombok.Value;

@Value
public class PayoutErrorReceivedEvent {
    UUID hedvigOrderId;

    Error error;
}
