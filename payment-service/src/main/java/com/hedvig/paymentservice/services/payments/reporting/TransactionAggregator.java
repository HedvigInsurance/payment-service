package com.hedvig.paymentservice.services.payments.reporting;

public interface TransactionAggregator {
  MonthlyTransactionsAggregations aggregateAllChargesMonthlyInSek();
}
