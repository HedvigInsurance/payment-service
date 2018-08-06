package com.hedvig.paymentservice.domain.trustlyOrder.commands;

import lombok.Value;

@Value
public class CreateAccountCommand {
  String accountId;
}
