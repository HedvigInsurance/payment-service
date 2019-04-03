package com.hedvig.paymentservice.services.payments.reporting;

import com.hedvig.paymentservice.domain.payments.TransactionType;
import com.hedvig.paymentservice.query.member.entities.Transaction;
import com.hedvig.paymentservice.query.member.entities.TransactionHistoryEvent;
import com.hedvig.paymentservice.query.member.entities.TransactionHistoryEventType;
import com.hedvig.paymentservice.services.payments.TransactionHistoryDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

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
  public MonthlyTransactionsAggregations aggregateAllChargesMonthlyInSek(final YearMonth period) {
    final Map<UUID, List<TransactionHistoryEvent>> historyEventsByTxId = transactionHistoryDao.findAllAsStream()
      .collect(groupingBy(TransactionHistoryEvent::getTransactionId));
    final Map<UUID, Transaction> transactionsById = transactionHistoryDao.findWithinPeriodAndWithTransactionIds(
      period,
      historyEventsByTxId.keySet()
    )
      .stream()
      .filter(isWithinPeriod(period))
      .collect(toMap(Transaction::getId, tx -> tx));
    final Map<UUID, ChargeSource> transactionInsuranceTypes = chargeSourceGuesser.guessChargesMetadata(transactionsById.values(), period);

    final List<TransactionHistoryEvent> allTxEvents = historyEventsByTxId.values().stream()
      .filter(this::hasNoFailedEvents)
      .filter(this::hasCompleted)
      .filter(isCharge(transactionsById))
      .flatMap(transactionHistoryEvents ->
        transactionHistoryEvents.stream()
          .filter(event -> event.getType().equals(TransactionHistoryEventType.COMPLETED)))
      .collect(toList());

    final Map<Year, BigDecimal> studentAggregation = aggregateByUnderwritingYear(
      allTxEvents.stream()
      .filter(txe -> transactionInsuranceTypes.get(txe.getTransactionId()).equals(ChargeSource.STUDENT_INSURANCE))
    );
    final Map<Year, BigDecimal> householdAggregation = aggregateByUnderwritingYear(allTxEvents.stream()
      .filter(txe -> transactionInsuranceTypes.get(txe.getTransactionId()).equals(ChargeSource.HOUSEHOLD_INSURANCE))
    );
    final Map<Year, BigDecimal> totalAggregation = aggregateByUnderwritingYear(allTxEvents.stream());
    return new MonthlyTransactionsAggregations(studentAggregation, householdAggregation, totalAggregation);
  }

  private Map<Year, BigDecimal> aggregateByUnderwritingYear(final Stream<TransactionHistoryEvent> txes) {
    return txes
      .collect(groupingBy(txe -> Year.from(txe.getTime().atZone(ZoneId.of("Europe/Stockholm")).toLocalDate())))
      .entrySet().stream()
      .collect(toMap(
        Map.Entry::getKey,
        entry -> entry.getValue().stream()
          .map(TransactionHistoryEvent::getAmount)
          .filter(Objects::nonNull)
          .reduce(BigDecimal.ZERO, BigDecimal::add)
      ));
  }

  private Predicate<Transaction> isWithinPeriod(final YearMonth period) {
    return transaction -> {
      final LocalDate txeDate = transaction.getTimestamp().atZone(ZoneId.of("Europe/Stockholm")).toLocalDate();
      return YearMonth.from(txeDate).equals(period);
    };
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
    return (transactionHistoryEvents) -> Optional.ofNullable(transactionsById.get(transactionHistoryEvents.get(0).getTransactionId()))
      .map(
        transaction -> transaction
          .getTransactionType()
          .equals(TransactionType.CHARGE)
      )
      .orElse(false);
  }
}
