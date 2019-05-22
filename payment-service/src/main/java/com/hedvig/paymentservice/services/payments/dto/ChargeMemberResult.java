package com.hedvig.paymentservice.services.payments.dto;

import lombok.Value;

import java.util.UUID;

@Value
public class ChargeMemberResult {
  UUID transactionId;
  ChargeMemberResultType type;
}
