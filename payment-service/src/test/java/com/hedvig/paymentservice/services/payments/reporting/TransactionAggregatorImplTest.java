package com.hedvig.paymentservice.services.payments.reporting;

import com.hedvig.paymentservice.domain.payments.TransactionType;
import com.hedvig.paymentservice.query.member.entities.Transaction;
import com.hedvig.paymentservice.query.member.entities.TransactionHistoryEntity;
import com.hedvig.paymentservice.query.member.entities.TransactionHistoryEventType;
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.PolicyGuessResponseDto;
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.PolicyType;
import com.hedvig.paymentservice.services.payments.TransactionHistoryDao;
import org.javamoney.moneta.Money;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransactionAggregatorImplTest {
  private Set<Transaction> transactions;
  private Map<UUID, Optional<PolicyGuessResponseDto>> transactionGuesses;

  @Before
  public void setUp() {
    transactions = new HashSet<>();
    transactionGuesses = new HashMap<>();
  }

  @Test
  public void aggregatesCompletedTransactions() {
    final UUID anId = UUID.randomUUID();
    final Stream<TransactionHistoryEntity> transactionHistory = Stream.of(
      buildTransactionHistoryEntity(BigDecimal.TEN, "2019-02-01T13:37:00.0Z", UUID.randomUUID(), TransactionHistoryEventType.COMPLETED, null, TransactionType.CHARGE, PolicyType.BRF), // Good February tx
      buildTransactionHistoryEntity(BigDecimal.ONE, "2019-02-01T13:37:00.0Z", anId, TransactionHistoryEventType.COMPLETED, "2019-01-31T13:37:00.0Z", TransactionType.CHARGE, PolicyType.STUDENT_BRF), // Tx initialised on 01-31 but completed 02-01
      buildTransactionHistoryEntity(BigDecimal.ONE, "2019-01-31T13:37:00.0Z", anId, TransactionHistoryEventType.CREATED, null, TransactionType.CHARGE, PolicyType.STUDENT_BRF), // Tx initialised on 01-31 but completed 02-01
      buildTransactionHistoryEntity(BigDecimal.ONE, "2019-02-01T13:37:00.0Z", UUID.randomUUID(), TransactionHistoryEventType.CREATED, null, TransactionType.CHARGE, PolicyType.BRF), // Not completed
      buildTransactionHistoryEntity(BigDecimal.TEN, "2019-01-01T13:37:00.0Z", UUID.randomUUID(), TransactionHistoryEventType.COMPLETED, null, TransactionType.CHARGE, PolicyType.BRF) // Good, but in January tx
    );

    final TransactionHistoryDao transactionHistoryDaoStub = mock(TransactionHistoryDao.class);
    final ChargeSourceGuesser chargeSourceGuesserStub = mock(ChargeSourceGuesser.class);
    final TransactionAggregator transactionAggregator = new TransactionAggregatorImpl(transactionHistoryDaoStub, chargeSourceGuesserStub);

    final YearMonth period = YearMonth.of(2019, 1);

    when(transactionHistoryDaoStub.findAllAsStream()).thenReturn(transactionHistory);
    when(transactionHistoryDaoStub.findWithinPeriodAndWithTransactionIds(eq(period), any(Set.class))).thenReturn(transactions);
    when(chargeSourceGuesserStub.guessChargesMetadata(any(), eq(period))).thenReturn(transactionGuesses);

    final MonthlyTransactionsAggregations aggregations = transactionAggregator.aggregateAllChargesMonthlyInSek(period);

    assertThat(aggregations.getTotal().get(Year.of(2019))).isEqualTo(BigDecimal.valueOf(11));
  }

  @Test
  public void doesntAggregatePayouts() {
    final Stream<TransactionHistoryEntity> transactionHistory = Stream.of(
      buildTransactionHistoryEntity(BigDecimal.TEN, "2019-02-01T13:37:00.0Z", UUID.randomUUID(), TransactionHistoryEventType.COMPLETED, null, TransactionType.CHARGE, PolicyType.BRF),
      buildTransactionHistoryEntity(BigDecimal.ONE, "2019-02-01T13:37:00.0Z", UUID.randomUUID(), TransactionHistoryEventType.COMPLETED, null, TransactionType.PAYOUT, PolicyType.BRF)
    );

    final TransactionHistoryDao transactionHistoryDaoStub = mock(TransactionHistoryDao.class);
    final ChargeSourceGuesser chargeSourceGuesserStub = mock(ChargeSourceGuesser.class);
    final TransactionAggregator transactionAggregator = new TransactionAggregatorImpl(transactionHistoryDaoStub, chargeSourceGuesserStub);

    final YearMonth period = YearMonth.of(2019, 2);

    when(transactionHistoryDaoStub.findAllAsStream()).thenReturn(transactionHistory);
    when(transactionHistoryDaoStub.findWithinPeriodAndWithTransactionIds(eq(period), any(Set.class))).thenReturn(transactions);
    when(chargeSourceGuesserStub.guessChargesMetadata(any(), eq(period))).thenReturn(transactionGuesses);

    final MonthlyTransactionsAggregations aggregations = transactionAggregator.aggregateAllChargesMonthlyInSek(period);
    assertThat(aggregations.getTotal().get(Year.of(2019))).isEqualTo(BigDecimal.TEN);
  }

  @Test
  public void doesntAggregateFailedCharges_evenIfTheyreReportedAsCompleted_maybeWrongIdk() {
    final UUID anId = UUID.randomUUID();

    final Stream<TransactionHistoryEntity> transactionHistory = Stream.of(
      buildTransactionHistoryEntity(BigDecimal.TEN, "2019-02-01T13:37:00.0Z", anId, TransactionHistoryEventType.COMPLETED, null, TransactionType.CHARGE, PolicyType.BRF),
      buildTransactionHistoryEntity(BigDecimal.TEN, "2019-02-01T13:37:00.0Z", anId, TransactionHistoryEventType.FAILED, null, TransactionType.CHARGE, PolicyType.BRF)
    );

    final TransactionHistoryDao transactionHistoryDaoStub = mock(TransactionHistoryDao.class);
    final ChargeSourceGuesser chargeSourceGuesserStub = mock(ChargeSourceGuesser.class);
    final TransactionAggregator transactionAggregator = new TransactionAggregatorImpl(transactionHistoryDaoStub, chargeSourceGuesserStub);

    final YearMonth period = YearMonth.of(2019, 2);

    when(transactionHistoryDaoStub.findAllAsStream()).thenReturn(transactionHistory);
    when(transactionHistoryDaoStub.findWithinPeriodAndWithTransactionIds(eq(period), any(Set.class))).thenReturn(transactions);
    when(chargeSourceGuesserStub.guessChargesMetadata(any(), eq(period))).thenReturn(transactionGuesses);

    final MonthlyTransactionsAggregations aggregations = transactionAggregator.aggregateAllChargesMonthlyInSek(period);

    assertThat(aggregations.getTotal()).hasSize(0);
  }

  private TransactionHistoryEntity buildTransactionHistoryEntity(final BigDecimal amount, final String time, final UUID transactionId, final TransactionHistoryEventType type, final String transactionTime, final TransactionType transactionType, final PolicyType policyType) {
    final Instant theTimeDefinitely = Instant.parse(transactionTime == null ? time : transactionTime);
    final Transaction transactionStub = mock(Transaction.class);
    when(transactionStub.getId()).thenReturn(transactionId == null ? UUID.randomUUID() : transactionId);
    when(transactionStub.getTimestamp()).thenReturn(theTimeDefinitely);
    when(transactionStub.getMoney()).thenReturn(Money.of(amount, "SEK"));
    when(transactionStub.getTransactionType()).thenReturn(transactionType);

    final Optional<UUID> existingIdMaybe = transactions.stream()
      .map(Transaction::getId)
      .filter(existingTx -> existingTx.equals(transactionStub.getId()))
      .findFirst();
    if (!existingIdMaybe.isPresent()) {
      transactions.add(transactionStub);
    }
    transactionGuesses.put(
      transactionId,
      Optional.of(new PolicyGuessResponseDto(policyType, theTimeDefinitely.atZone(ZoneId.of("Europe/Stockholm")).toLocalDate()))
    );

    return new TransactionHistoryEntity(
      transactionStub.getId(),
      amount,
      "SEK",
      Instant.parse(time),
      type,
      null
    );
  }
}
