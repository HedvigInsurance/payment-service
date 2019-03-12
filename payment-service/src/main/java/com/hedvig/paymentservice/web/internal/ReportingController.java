package com.hedvig.paymentservice.web.internal;

import com.hedvig.paymentservice.services.payments.reporting.MonthlyTransactionsAggregations;
import com.hedvig.paymentservice.services.payments.reporting.TransactionAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/_/reporting")
public class ReportingController {
  private final TransactionAggregator transactionAggregator;

  @Autowired
  public ReportingController(final TransactionAggregator transactionAggregator) {
    this.transactionAggregator = transactionAggregator;
  }

  @GetMapping(path = {"monthlyEarnedGrossPremium"})
  public MonthlyTransactionsAggregations getMonthlyEarnedGrossPremium() {
    return transactionAggregator.aggregateAllChargesMonthlyInSek();
  }
}
