package com.hedvig.paymentservice.services.payments.reporting;

import com.hedvig.paymentservice.domain.payments.TransactionType;
import com.hedvig.paymentservice.query.member.entities.Transaction;
import com.hedvig.paymentservice.query.member.entities.TransactionHistoryEvent;
import com.hedvig.paymentservice.query.member.entities.TransactionHistoryEventType;
import com.hedvig.paymentservice.services.payments.TransactionHistoryDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
    final Map<UUID, Transaction> transactionsById = transactionHistoryDao.findTransactionsAsStream(historyEventsByTxId.keySet())
      .collect(Collectors.toMap(Transaction::getId, tx -> tx));

    return historyEventsByTxId.values().stream()
      .filter(this::hasNoFailedEvents)
      .filter(this::hasCompleted)
      .filter(isCharge(transactionsById))
      .flatMap(transactionHistoryEvents ->
        transactionHistoryEvents.stream()
          .filter(event -> event.getType().equals(TransactionHistoryEventType.COMPLETED)))
      .collect(HashMap::new, accumulateTransactionsMonthly(transactionsById), Map::putAll);
  }

  private boolean hasNoFailedEvents(final List<TransactionHistoryEvent> transactionHistoryEvents) {
    return transactionHistoryEvents.parallelStream().noneMatch(event -> event.getType().equals(TransactionHistoryEventType.FAILED));
  }

  private boolean hasCompleted(final List<TransactionHistoryEvent> transactionHistoryEvents) {
    return transactionHistoryEvents.parallelStream().anyMatch(event -> event.getType().equals(TransactionHistoryEventType.COMPLETED));
  }

  private Predicate<List<TransactionHistoryEvent>> isCharge(final Map<UUID, Transaction> transactionsById) {
    return (transactionHistoryEvents) -> transactionsById.get(transactionHistoryEvents.get(0).getTransactionId())
      .getTransactionType()
      .equals(TransactionType.CHARGE);
  }

  private BiConsumer<Map<YearMonth, BigDecimal>, TransactionHistoryEvent> accumulateTransactionsMonthly(final Map<UUID, Transaction> transactionsById) {
    return (monthlyAggregation, txe) -> {
      final YearMonth yearMonth = YearMonth.from(txe.getTime().atOffset(ZoneOffset.UTC));
      final BigDecimal currentValue = Optional.ofNullable(monthlyAggregation.get(yearMonth)).orElse(BigDecimal.ZERO);
      final BigDecimal transactionValue = transactionsById.get(txe.getTransactionId()).getMoney().getNumber().numberValue(BigDecimal.class);
      monthlyAggregation.put(yearMonth, currentValue.add(transactionValue));
    };
  }
}
