package com.hedvig.paymentservice.services.payments.reporting;

import com.hedvig.paymentservice.query.member.entities.TransactionHistoryEventType;
import com.hedvig.paymentservice.query.member.entities.Transaction;
import com.hedvig.paymentservice.query.member.entities.TransactionHistoryEvent;
import com.hedvig.paymentservice.services.payments.TransactionHistoryDao;
import org.javamoney.moneta.Money;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransactionAggregatorImplTest {
  @Test
  public void aggregatesChargesMonthlyCorrectly() {
    final UUID anId = UUID.randomUUID();
    final Stream<TransactionHistoryEvent> transactionHistory = Stream.of(
      buildTransactionHistoryEvent(BigDecimal.TEN, "2019-02-01T13:37:00.0Z", UUID.randomUUID(), TransactionHistoryEventType.COMPLETED, null),
      buildTransactionHistoryEvent(BigDecimal.ONE, "2019-02-01T13:37:00.0Z", anId, TransactionHistoryEventType.COMPLETED, "2019-01-31T13:37:00.0Z"), // Transaction initialised on 01-31 but completed 02-01
      buildTransactionHistoryEvent(BigDecimal.ONE, "2019-01-31T13:37:00.0Z", anId, TransactionHistoryEventType.CREATED, null),
      buildTransactionHistoryEvent(BigDecimal.ONE, "2019-02-01T13:37:00.0Z", UUID.randomUUID(), TransactionHistoryEventType.CREATED, null),
      buildTransactionHistoryEvent(BigDecimal.TEN, "2019-01-01T13:37:00.0Z", UUID.randomUUID(), TransactionHistoryEventType.COMPLETED, null),
      buildTransactionHistoryEvent(BigDecimal.ONE, "2019-03-01T13:37:00.0Z", UUID.randomUUID(), TransactionHistoryEventType.COMPLETED, null)
    );

    final TransactionHistoryDao transactionHistoryDaoStub = mock(TransactionHistoryDao.class);
    final TransactionAggregator transactionAggregator = new TransactionAggregatorImpl(transactionHistoryDaoStub);

    when(transactionHistoryDaoStub.findAllAsStream()).thenReturn(transactionHistory);

    final Map<YearMonth, BigDecimal> monthlyAggregation = transactionAggregator.aggregateAllChargesMonthlyInSek();

    assertEquals(BigDecimal.TEN, monthlyAggregation.get(YearMonth.parse("2019-01")));
    assertEquals(BigDecimal.valueOf(11), monthlyAggregation.get(YearMonth.parse("2019-02")));
    assertEquals(BigDecimal.ONE, monthlyAggregation.get(YearMonth.parse("2019-03")));
  }

  private TransactionHistoryEvent buildTransactionHistoryEvent(final BigDecimal amount, final String time, final UUID transactionId, final TransactionHistoryEventType type, final String transactionTime) {
    final Transaction transactionStub = mock(Transaction.class);
    when(transactionStub.getId()).thenReturn(transactionId == null ? UUID.randomUUID() : transactionId);
    when(transactionStub.getTimestamp()).thenReturn(Instant.parse(transactionTime == null ? time : transactionTime));
    when(transactionStub.getMoney()).thenReturn(Money.of(amount, "SEK"));

    return new TransactionHistoryEvent(
      transactionStub,
      amount,
      "SEK",
      Instant.parse(time),
      type,
      null
    );
  }
}
