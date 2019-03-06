package com.hedvig.paymentservice.services.payments.reporting;

import com.hedvig.paymentservice.query.member.entities.TransactionHistoryEventType;
import com.hedvig.paymentservice.query.member.entities.Transaction;
import com.hedvig.paymentservice.query.member.entities.TransactionHistoryEvent;
import com.hedvig.paymentservice.services.payments.TransactionHistoryDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Service
public class TransactionAggregatorImpl implements TransactionAggregator {
  private final TransactionHistoryDao transactionHistoryDao;

  @Autowired
  public TransactionAggregatorImpl(final TransactionHistoryDao transactionHistoryDao) {
    this.transactionHistoryDao = transactionHistoryDao;
  }

  @Override
  public Map<YearMonth, BigDecimal> aggregateAllChargesMonthlyInSek() {
    final Map<UUID, List<TransactionHistoryEvent>> historyEventsByTxId = transactionHistoryDao.findAllAsStream()
      .collect(Collectors.groupingBy(TransactionHistoryEvent::getTransactionId));

    return historyEventsByTxId.values().stream()
      .filter(this::hasNoFailedEvents)
      .filter(this::hasCompleted)
      .map(transactionHistoryEvents ->
        transactionHistoryEvents.stream()
          .filter(event -> event.getType().equals(TransactionHistoryEventType.COMPLETED))
          .findFirst()
          .get())
      .collect(HashMap::new, this::accumulateTransactionsMonthly, HashMap::putAll);
  }

  private boolean hasNoFailedEvents(final List<TransactionHistoryEvent> transactionHistoryEvents) {
    return transactionHistoryEvents.parallelStream().noneMatch(event -> event.getType().equals(TransactionHistoryEventType.FAILED));
  }

  private boolean hasCompleted(final List<TransactionHistoryEvent> transactionHistoryEvents) {
    return transactionHistoryEvents.parallelStream().anyMatch(event -> event.getType().equals(TransactionHistoryEventType.COMPLETED));
  }

  private void accumulateTransactionsMonthly(final Map<YearMonth, BigDecimal> monthlyAggregation, final TransactionHistoryEvent txe) {
    final YearMonth yearMonth = YearMonth.from(txe.getTime().atOffset(ZoneOffset.UTC));
    final BigDecimal currentValue = Optional.ofNullable(monthlyAggregation.get(yearMonth)).orElse(BigDecimal.ZERO);
    final BigDecimal transactionValue = requireNonNull(txe.getTransaction().getMoney()).getNumber().numberValue(BigDecimal.class);
    monthlyAggregation.put(yearMonth, currentValue.add(transactionValue));
  }
}
