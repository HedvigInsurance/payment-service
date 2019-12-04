package com.hedvig.paymentservice.services.payments.reporting;

import lombok.Value;

import java.math.BigDecimal;
import java.time.Year;
import java.util.Map;

@Value
public class MonthlyTransactionsAggregations {
  private Map<Year, BigDecimal> student;
  private Map<Year, BigDecimal> household;
  private Map<Year, BigDecimal> house;
  private Map<Year, BigDecimal> total;
}
