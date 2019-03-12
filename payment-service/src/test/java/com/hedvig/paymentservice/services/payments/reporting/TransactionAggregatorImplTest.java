package com.hedvig.paymentservice.services.payments.reporting;

import com.hedvig.paymentservice.domain.payments.TransactionType;
import com.hedvig.paymentservice.query.member.entities.Transaction;
import com.hedvig.paymentservice.query.member.entities.TransactionHistoryEvent;
import com.hedvig.paymentservice.query.member.entities.TransactionHistoryEventType;
import com.hedvig.paymentservice.services.payments.TransactionHistoryDao;
import org.javamoney.moneta.Money;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransactionAggregatorImplTest {
  private List<Transaction> transactions;
  private Map<UUID, ChargeSource> transactionSources;

  @Before
  public void setUp() {
    transactions = new ArrayList<>();
    transactionSources = new HashMap<>();
  }

  @Test
  public void aggregatesCompletedTransactions() {
    final UUID anId = UUID.randomUUID();
    final Stream<TransactionHistoryEvent> transactionHistory = Stream.of(
      buildTransactionHistoryEvent(BigDecimal.TEN, "2019-02-01T13:37:00.0Z", UUID.randomUUID(), TransactionHistoryEventType.COMPLETED, null, TransactionType.CHARGE, ChargeSource.HOUSEHOLD_INSURANCE), // Good February tx
      buildTransactionHistoryEvent(BigDecimal.ONE, "2019-02-01T13:37:00.0Z", anId, TransactionHistoryEventType.COMPLETED, "2019-01-31T13:37:00.0Z", TransactionType.CHARGE, ChargeSource.STUDENT_INSURANCE), // Tx initialised on 01-31 but completed 02-01
      buildTransactionHistoryEvent(BigDecimal.ONE, "2019-01-31T13:37:00.0Z", anId, TransactionHistoryEventType.CREATED, null, TransactionType.CHARGE, ChargeSource.STUDENT_INSURANCE), // Tx initialised on 01-31 but completed 02-01
      buildTransactionHistoryEvent(BigDecimal.ONE, "2019-02-01T13:37:00.0Z", UUID.randomUUID(), TransactionHistoryEventType.CREATED, null, TransactionType.CHARGE, ChargeSource.HOUSEHOLD_INSURANCE), // Not completed
      buildTransactionHistoryEvent(BigDecimal.TEN, "2019-01-01T13:37:00.0Z", UUID.randomUUID(), TransactionHistoryEventType.COMPLETED, null, TransactionType.CHARGE, ChargeSource.HOUSEHOLD_INSURANCE), // Good January tx
      buildTransactionHistoryEvent(BigDecimal.ONE, "2100-03-01T13:37:00.0Z", UUID.randomUUID(), TransactionHistoryEventType.COMPLETED, null, TransactionType.CHARGE, ChargeSource.HOUSEHOLD_INSURANCE) // Good future TX
    );

    final TransactionHistoryDao transactionHistoryDaoStub = mock(TransactionHistoryDao.class);
    final ChargeSourceGuesser chargeSourceGuesserStub = mock(ChargeSourceGuesser.class);
    final TransactionAggregator transactionAggregator = new TransactionAggregatorImpl(transactionHistoryDaoStub, chargeSourceGuesserStub);

    when(transactionHistoryDaoStub.findAllAsStream()).thenReturn(transactionHistory);
    when(transactionHistoryDaoStub.findTransactionsAsStream(any())).thenReturn(transactions.stream());
    when(chargeSourceGuesserStub.guessChargesInsuranceTypes(any())).thenReturn(transactionSources);

    final MonthlyTransactionsAggregations aggregations = transactionAggregator.aggregateAllChargesMonthlyInSek();

    assertEquals(3, aggregations.getTotal().size());
    assertEquals(BigDecimal.TEN, aggregations.getTotal().get(YearMonth.parse("2019-01")));
    assertEquals(BigDecimal.valueOf(11), aggregations.getTotal().get(YearMonth.parse("2019-02")));
    assertEquals(BigDecimal.ONE, aggregations.getTotal().get(YearMonth.parse("2100-03")));

    assertEquals(3, aggregations.getHousehold().size());
    assertEquals(BigDecimal.TEN, aggregations.getHousehold().get(YearMonth.parse("2019-01")));
    assertEquals(BigDecimal.TEN, aggregations.getHousehold().get(YearMonth.parse("2019-02")));
    assertEquals(BigDecimal.ONE, aggregations.getHousehold().get(YearMonth.parse("2100-03")));

    assertEquals(1, aggregations.getStudent().size());
    assertEquals(BigDecimal.ONE, aggregations.getStudent().get(YearMonth.parse("2019-02")));
  }

  @Test
  public void doesntAggregatePayouts() {
    final Stream<TransactionHistoryEvent> transactionHistory = Stream.of(
      buildTransactionHistoryEvent(BigDecimal.TEN, "2019-02-01T13:37:00.0Z", UUID.randomUUID(), TransactionHistoryEventType.COMPLETED, null, TransactionType.CHARGE, ChargeSource.HOUSEHOLD_INSURANCE),
      buildTransactionHistoryEvent(BigDecimal.ONE, "2019-02-01T13:37:00.0Z", UUID.randomUUID(), TransactionHistoryEventType.COMPLETED, null, TransactionType.PAYOUT, ChargeSource.HOUSEHOLD_INSURANCE)
    );

    final TransactionHistoryDao transactionHistoryDaoStub = mock(TransactionHistoryDao.class);
    final ChargeSourceGuesser chargeSourceGuesserStub = mock(ChargeSourceGuesser.class);
    final TransactionAggregator transactionAggregator = new TransactionAggregatorImpl(transactionHistoryDaoStub, chargeSourceGuesserStub);

    when(transactionHistoryDaoStub.findAllAsStream()).thenReturn(transactionHistory);
    when(transactionHistoryDaoStub.findTransactionsAsStream(any())).thenReturn(transactions.stream());
    when(chargeSourceGuesserStub.guessChargesInsuranceTypes(any())).thenReturn(transactionSources);

    final MonthlyTransactionsAggregations aggregations = transactionAggregator.aggregateAllChargesMonthlyInSek();
    assertEquals(1, aggregations.getHousehold().size());
    assertEquals(1, aggregations.getTotal().size());
    assertEquals(0, aggregations.getStudent().size());
    assertEquals(BigDecimal.TEN, aggregations.getTotal().get(YearMonth.parse("2019-02")));
  }

  @Test
  public void doesntAggregateFailedCharges_evenIfTheyreReportedAsCompleted_maybeWrongIdk() {
    final UUID anId = UUID.randomUUID();

    final Stream<TransactionHistoryEvent> transactionHistory = Stream.of(
      buildTransactionHistoryEvent(BigDecimal.TEN, "2019-02-01T13:37:00.0Z", anId, TransactionHistoryEventType.COMPLETED, null, TransactionType.CHARGE, ChargeSource.HOUSEHOLD_INSURANCE),
      buildTransactionHistoryEvent(BigDecimal.TEN, "2019-02-01T13:37:00.0Z", anId, TransactionHistoryEventType.FAILED, null, TransactionType.CHARGE, ChargeSource.HOUSEHOLD_INSURANCE)
    );

    final TransactionHistoryDao transactionHistoryDaoStub = mock(TransactionHistoryDao.class);
    final ChargeSourceGuesser chargeSourceGuesserStub = mock(ChargeSourceGuesser.class);
    final TransactionAggregator transactionAggregator = new TransactionAggregatorImpl(transactionHistoryDaoStub, chargeSourceGuesserStub);

    when(transactionHistoryDaoStub.findAllAsStream()).thenReturn(transactionHistory);
    when(transactionHistoryDaoStub.findTransactionsAsStream(any())).thenReturn(transactions.stream());
    when(chargeSourceGuesserStub.guessChargesInsuranceTypes(any())).thenReturn(transactionSources);

//    final Map<YearMonth, BigDecimal> monthlyAggregation = transactionAggregator.aggregateAllChargesMonthlyInSek();
    final MonthlyTransactionsAggregations aggregations = transactionAggregator.aggregateAllChargesMonthlyInSek();

    assertEquals(0, aggregations.getTotal().size());
  }

  private TransactionHistoryEvent buildTransactionHistoryEvent(final BigDecimal amount, final String time, final UUID transactionId, final TransactionHistoryEventType type, final String transactionTime, final TransactionType transactionType, final ChargeSource chargeSource) {
    final Transaction transactionStub = mock(Transaction.class);
    when(transactionStub.getId()).thenReturn(transactionId == null ? UUID.randomUUID() : transactionId);
    when(transactionStub.getTimestamp()).thenReturn(Instant.parse(transactionTime == null ? time : transactionTime));
    when(transactionStub.getMoney()).thenReturn(Money.of(amount, "SEK"));
    when(transactionStub.getTransactionType()).thenReturn(transactionType);

    final Optional<UUID> existingIdMaybe = transactions.stream()
      .map(Transaction::getId)
      .filter(existingTx -> existingTx.equals(transactionStub.getId()))
      .findFirst();
    if (!existingIdMaybe.isPresent()) {
      transactions.add(transactionStub);
    }
    transactionSources.put(transactionId, chargeSource);

    return new TransactionHistoryEvent(
      transactionStub.getId(),
      amount,
      "SEK",
      Instant.parse(time),
      type,
      null
    );
  }
}
