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
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
public class TransactionAggregatorImpl implements TransactionAggregator {
  private final TransactionHistoryDao transactionHistoryDao;
  private final ChargeSourceGuesser chargeSourceGuesser;

  @Autowired
  public TransactionAggregatorImpl(final TransactionHistoryDao transactionHistoryDao, final ChargeSourceGuesser chargeSourceGuesser) {
    this.transactionHistoryDao = transactionHistoryDao;
    this.chargeSourceGuesser = chargeSourceGuesser;
  }

  @Override
  public MonthlyTransactionsAggregations aggregateAllChargesMonthlyInSek() {
    final Map<UUID, List<TransactionHistoryEvent>> historyEventsByTxId = transactionHistoryDao.findAllAsStream()
      .collect(Collectors.groupingBy(TransactionHistoryEvent::getTransactionId));
    final Map<UUID, Transaction> transactionsById = transactionHistoryDao.findTransactionsAsStream(historyEventsByTxId.keySet())
      .collect(Collectors.toMap(Transaction::getId, tx -> tx));
    final Map<UUID, ChargeSource> transactionInsuranceTypes = chargeSourceGuesser.guessChargesInsuranceTypes(transactionsById.values());

    final List<TransactionHistoryEvent> allTxEvents = historyEventsByTxId.values().stream()
      .filter(this::hasNoFailedEvents)
      .filter(this::hasCompleted)
      .filter(isCharge(transactionsById))
      .flatMap(transactionHistoryEvents ->
        transactionHistoryEvents.stream()
          .filter(event -> event.getType().equals(TransactionHistoryEventType.COMPLETED)))
      .collect(toList());

    final Map<YearMonth, BigDecimal> studentAggregation = allTxEvents.stream()
      .filter(txe -> transactionInsuranceTypes.get(txe.getTransactionId()).equals(ChargeSource.STUDENT_INSURANCE))
      .collect(HashMap::new, accumulateTransactionsMonthly(transactionsById), Map::putAll);
    final Map<YearMonth, BigDecimal> householdAggregation = allTxEvents.stream()
      .filter(txe -> transactionInsuranceTypes.get(txe.getTransactionId()).equals(ChargeSource.HOUSEHOLD_INSURANCE))
      .collect(HashMap::new, accumulateTransactionsMonthly(transactionsById), Map::putAll);
    final Map<YearMonth, BigDecimal> totalAggregation = allTxEvents.stream().collect(HashMap::new, accumulateTransactionsMonthly(transactionsById), Map::putAll);

    return new MonthlyTransactionsAggregations(studentAggregation, householdAggregation, totalAggregation);
  }

  private boolean hasNoFailedEvents(final List<TransactionHistoryEvent> transactionHistoryEvents) {
    return transactionHistoryEvents.parallelStream().noneMatch(
      event ->
        event.getType().equals(TransactionHistoryEventType.FAILED)
          || event.getType().equals(TransactionHistoryEventType.ERROR)
    );
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
