package com.hedvig.paymentservice.web.dtos;

import lombok.Value;

import javax.money.MonetaryAmount;

@Value
public class ChargeRequest {
    MonetaryAmount amount;
}
