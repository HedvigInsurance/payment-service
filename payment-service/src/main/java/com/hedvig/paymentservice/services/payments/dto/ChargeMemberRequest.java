package com.hedvig.paymentservice.services.payments.dto;

import lombok.Value;

import javax.money.MonetaryAmount;

@Value
public class ChargeMemberRequest {
    String memberId;
    MonetaryAmount amount;
}
