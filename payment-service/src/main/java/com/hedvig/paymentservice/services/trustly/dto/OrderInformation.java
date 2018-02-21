package com.hedvig.paymentservice.services.trustly.dto;

import com.hedvig.paymentservice.domain.trustlyOrder.OrderState;
import lombok.Value;

import java.util.UUID;

@Value
public class OrderInformation {

    UUID id;

    String iframeUrl;

    OrderState state;

}
