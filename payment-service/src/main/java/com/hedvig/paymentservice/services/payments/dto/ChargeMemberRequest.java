package com.hedvig.paymentservice.services.payments.dto;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;

import lombok.Value;

@Value
public class ChargeMemberRequest {
    String memberId;
    MonetaryAmount amount;
    CurrencyUnit currency;
    String email;
}
