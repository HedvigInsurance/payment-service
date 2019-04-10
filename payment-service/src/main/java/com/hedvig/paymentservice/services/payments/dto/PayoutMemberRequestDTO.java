package com.hedvig.paymentservice.services.payments.dto;

import com.hedvig.paymentservice.domain.payments.TransactionCategory;
import lombok.Value;

import javax.money.MonetaryAmount;

@Value
public class PayoutMemberRequestDTO {
  MonetaryAmount amount;
  TransactionCategory category;
  String referenceId;
  String note;
}
