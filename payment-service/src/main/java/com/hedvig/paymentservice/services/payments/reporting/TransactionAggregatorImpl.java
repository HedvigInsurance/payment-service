package com.hedvig.paymentservice.services.payments.reporting;

import com.hedvig.paymentservice.domain.payments.TransactionType;
import com.hedvig.paymentservice.query.member.entities.Transaction;
import com.hedvig.paymentservice.query.member.entities.TransactionHistoryEntity;
import com.hedvig.paymentservice.query.member.entities.TransactionHistoryEventType;
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.PolicyGuessResponseDto;
import com.hedvig.paymentservice.services.payments.TransactionHistoryDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

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
    final Map<UUID, List<TransactionHistoryEntity>> historyEntitiesByTxId = transactionHistoryDao.findAllAsStream()
      .collect(groupingBy(TransactionHistoryEntity::getTransactionId));
    final Map<UUID, Transaction> transactionsById = transactionHistoryDao.findWithinPeriodAndWithTransactionIds(
      period,
      historyEntitiesByTxId.keySet()
    )
      .stream()
      .filter(isWithinPeriod(period))
      .collect(toMap(Transaction::getId, tx -> tx));
    final Map<UUID, Optional<PolicyGuessResponseDto>> transactionGuesses = chargeSourceGuesser.guessChargesMetadata(transactionsById.values(), period);
    final Map<UUID, ChargeSource> transactionChargeSources = transactionGuesses.entrySet().stream()
      .collect(toMap(
        Map.Entry::getKey,
        entry -> ChargeSource.from(entry.getValue().map(PolicyGuessResponseDto::getProductType))
      ));

    final List<TransactionHistoryEntity> allTxEvents = historyEntitiesByTxId.values().stream()
      .filter(this::hasNoFailedEvents)
      .filter(this::hasCompleted)
      .filter(isCharge(transactionsById))
      .flatMap(transactionHistoryEvents ->
        transactionHistoryEvents.stream()
          .filter(event -> event.getType().equals(TransactionHistoryEventType.COMPLETED)))
      .collect(toList());

    final Map<Year, BigDecimal> studentAggregation = aggregateByUnderwritingYear(
      allTxEvents.stream()
        .filter(txe -> transactionChargeSources.get(txe.getTransactionId()).equals(ChargeSource.STUDENT_INSURANCE)),
      transactionGuesses
    );
    final Map<Year, BigDecimal> householdAggregation = aggregateByUnderwritingYear(allTxEvents.stream()
        .filter(txe -> transactionChargeSources.get(txe.getTransactionId()).equals(ChargeSource.HOUSEHOLD_INSURANCE)),
      transactionGuesses
    );
    final Map<Year, BigDecimal> houseAggregation = aggregateByUnderwritingYear(allTxEvents.stream()
        .filter(txe -> transactionChargeSources.get(txe.getTransactionId()).equals(ChargeSource.HOUSE_INSURANCE)),
      transactionGuesses
    );
    final Map<Year, BigDecimal> totalAggregation = aggregateByUnderwritingYear(allTxEvents.stream(), transactionGuesses);
    return new MonthlyTransactionsAggregations(studentAggregation, householdAggregation, houseAggregation, totalAggregation);
  }

  private Map<Year, BigDecimal> aggregateByUnderwritingYear(
    final Stream<TransactionHistoryEntity> txes,
    Map<UUID, Optional<PolicyGuessResponseDto>> transactionGuesses
  ) {
    return txes
      .collect(groupingBy(txe -> Year.from(
        transactionGuesses.get(txe.getTransactionId())
          .map(PolicyGuessResponseDto::getInceptionInStockholm)
          .orElseGet(() -> txe.getTime().atZone(ZoneId.of("Europe/Stockholm")).toLocalDate()))
      ))
      .entrySet().stream()
      .collect(toMap(
        Map.Entry::getKey,
        entry -> entry.getValue().stream()
          .map(TransactionHistoryEntity::getAmount)
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

  private boolean hasNoFailedEvents(final List<TransactionHistoryEntity> transactionHistoryEvents) {
    return transactionHistoryEvents.parallelStream().noneMatch(
      event ->
        event.getType().equals(TransactionHistoryEventType.FAILED)
          || event.getType().equals(TransactionHistoryEventType.ERROR)
    );
  }

  private boolean hasCompleted(final List<TransactionHistoryEntity> transactionHistoryEvents) {
    return transactionHistoryEvents.parallelStream().anyMatch(event -> event.getType().equals(TransactionHistoryEventType.COMPLETED));
  }

  private Predicate<List<TransactionHistoryEntity>> isCharge(final Map<UUID, Transaction> transactionsById) {
    return (transactionHistoryEvents) -> Optional.ofNullable(transactionsById.get(transactionHistoryEvents.get(0).getTransactionId()))
      .map(
        transaction -> transaction
          .getTransactionType()
          .equals(TransactionType.CHARGE)
      )
      .orElse(false);
  }
}
