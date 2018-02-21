package com.hedvig.paymentservice.domain.trustlyOrder.commands;

import lombok.Value;

import java.util.UUID;

@Value
public class CreateTrustlySelectAccountOrderCommand {

    String memberId;

    UUID requestId;
}
