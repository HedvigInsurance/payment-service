package com.hedvig.paymentservice.services.trustly.dto;

import com.hedvig.paymentservice.domain.trustlyOrder.OrderState;
import java.util.UUID;
import lombok.Value;

@Value
public class OrderInformation {

  UUID id;

  String iframeUrl;

  OrderState state;
}
