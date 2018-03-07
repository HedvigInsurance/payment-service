package com.hedvig.paymentservice.services.trustly.dto;


import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import lombok.Value;

@Value
public class PaymentRequest {
    String memberId;
    MonetaryAmount amount;
    CurrencyUnit currencyType;
    String accountId;
    String email;
}
