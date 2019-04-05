package com.hedvig.paymentservice.services.payments.reporting;

import java.time.YearMonth;

public interface TransactionAggregator {
  MonthlyTransactionsAggregations aggregateAllChargesMonthlyInSek(final YearMonth period);
}
