package com.hedvig.paymentservice.domain.trustlyOrder.commands;

import java.util.UUID;
import lombok.Value;

@Value
public class CreateOrderCommand {

  String memberId;

  UUID hedvigOrderId;
}
