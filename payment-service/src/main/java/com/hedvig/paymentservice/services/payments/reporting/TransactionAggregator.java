package com.hedvig.paymentservice.services.payments.reporting;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;

public interface TransactionAggregator {
  Map<YearMonth, BigDecimal> aggregateAllChargesMonthlyInSek();
}
