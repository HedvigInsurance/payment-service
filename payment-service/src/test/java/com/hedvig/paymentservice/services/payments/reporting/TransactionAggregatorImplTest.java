package com.hedvig.paymentservice.services.payments.reporting;

import com.hedvig.paymentservice.domain.payments.TransactionType;
import com.hedvig.paymentservice.query.member.entities.TransactionHistoryEventType;
import com.hedvig.paymentservice.query.member.entities.Transaction;
import com.hedvig.paymentservice.query.member.entities.TransactionHistoryEvent;
import com.hedvig.paymentservice.services.payments.TransactionHistoryDao;
import org.javamoney.moneta.Money;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransactionAggregatorImplTest {
  private List<Transaction> transactions;

  @Before
  public void setUp(){
    transactions = new ArrayList<>();
  }

  @Test
  public void aggregatesChargesMonthlyCorrectly() {
    final UUID anId = UUID.randomUUID();
    final Stream<TransactionHistoryEvent> transactionHistory = Stream.of(
      buildTransactionHistoryEvent(BigDecimal.TEN, "2019-02-01T13:37:00.0Z", UUID.randomUUID(), TransactionHistoryEventType.COMPLETED, null, TransactionType.CHARGE),
      buildTransactionHistoryEvent(BigDecimal.ONE, "2019-02-01T13:37:00.0Z", anId, TransactionHistoryEventType.COMPLETED, "2019-01-31T13:37:00.0Z", TransactionType.CHARGE), // Transaction initialised on 01-31 but completed 02-01
      buildTransactionHistoryEvent(BigDecimal.ONE, "2019-01-31T13:37:00.0Z", anId, TransactionHistoryEventType.CREATED, null, TransactionType.CHARGE),
      buildTransactionHistoryEvent(BigDecimal.ONE, "2019-02-01T13:37:00.0Z", UUID.randomUUID(), TransactionHistoryEventType.CREATED, null, TransactionType.CHARGE),
      buildTransactionHistoryEvent(BigDecimal.TEN, "2019-01-01T13:37:00.0Z", UUID.randomUUID(), TransactionHistoryEventType.COMPLETED, null, TransactionType.CHARGE),
      buildTransactionHistoryEvent(BigDecimal.ONE, "2019-03-01T13:37:00.0Z", UUID.randomUUID(), TransactionHistoryEventType.COMPLETED, null, TransactionType.CHARGE),
      buildTransactionHistoryEvent(BigDecimal.ONE, "2019-02-01T13:37:00.0Z", UUID.randomUUID(), TransactionHistoryEventType.COMPLETED, null, TransactionType.PAYOUT)
    );

    final TransactionHistoryDao transactionHistoryDaoStub = mock(TransactionHistoryDao.class);
    final TransactionAggregator transactionAggregator = new TransactionAggregatorImpl(transactionHistoryDaoStub);

    when(transactionHistoryDaoStub.findAllAsStream()).thenReturn(transactionHistory);
    when(transactionHistoryDaoStub.findTransactionsAsStream(any())).thenReturn(transactions.stream());

    final Map<YearMonth, BigDecimal> monthlyAggregation = transactionAggregator.aggregateAllChargesMonthlyInSek();

    assertEquals(BigDecimal.TEN, monthlyAggregation.get(YearMonth.parse("2019-01")));
    assertEquals(BigDecimal.valueOf(11), monthlyAggregation.get(YearMonth.parse("2019-02")));
    assertEquals(BigDecimal.ONE, monthlyAggregation.get(YearMonth.parse("2019-03")));
  }

  private TransactionHistoryEvent buildTransactionHistoryEvent(final BigDecimal amount, final String time, final UUID transactionId, final TransactionHistoryEventType type, final String transactionTime, final TransactionType transactionType) {
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
