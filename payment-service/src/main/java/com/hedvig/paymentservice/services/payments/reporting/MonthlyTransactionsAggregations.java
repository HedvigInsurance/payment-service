package com.hedvig.paymentservice.services.payments.reporting;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class MonthlyTransactionsAggregations {
  private BigDecimal student;
  private BigDecimal household;
  private BigDecimal total;
}
