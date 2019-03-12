package com.hedvig.paymentservice.services.payments.reporting;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

public class MonthlyTransactionsAggregations {
  private Map<YearMonth, BigDecimal> student = new HashMap<>();
  private Map<YearMonth, BigDecimal> household = new HashMap<>();
  private Map<YearMonth, BigDecimal> total = new HashMap<>();

  public MonthlyTransactionsAggregations() {
  }

  public MonthlyTransactionsAggregations(final Map<YearMonth, BigDecimal> student, final Map<YearMonth, BigDecimal> household, final Map<YearMonth, BigDecimal> total) {
    this.student = student;
    this.household = household;
    this.total = total;
  }

  public Map<YearMonth, BigDecimal> getStudent() {
    return student;
  }

  public MonthlyTransactionsAggregations setStudent(final Map<YearMonth, BigDecimal> student) {
    this.student = student;
    return this;
  }

  public Map<YearMonth, BigDecimal> getHousehold() {
    return household;
  }

  public MonthlyTransactionsAggregations setHousehold(final Map<YearMonth, BigDecimal> household) {
    this.household = household;
    return this;
  }

  public Map<YearMonth, BigDecimal> getTotal() {
    return total;
  }

  public MonthlyTransactionsAggregations setTotal(final Map<YearMonth, BigDecimal> total) {
    this.total = total;
    return this;
  }
}
