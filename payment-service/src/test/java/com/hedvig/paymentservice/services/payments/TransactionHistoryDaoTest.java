package com.hedvig.paymentservice.services.payments;

import com.hedvig.paymentservice.domain.payments.DirectDebitStatus;
import com.hedvig.paymentservice.domain.payments.TransactionStatus;
import com.hedvig.paymentservice.domain.payments.TransactionType;
import com.hedvig.paymentservice.query.member.entities.Member;
import com.hedvig.paymentservice.query.member.entities.Transaction;
import com.hedvig.paymentservice.query.member.entities.TransactionHistoryEventRepository;
import com.hedvig.paymentservice.query.member.entities.TransactionRepository;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TransactionHistoryDaoTest {


  @Mock
  private TransactionHistoryEventRepository transactionHistoryEventRepository;
  @Mock
  private TransactionRepository transactionRepository;

  private TransactionHistoryDao sut;

  @Before
  public void setUp() {
    sut = new TransactionHistoryDao(transactionHistoryEventRepository, transactionRepository);
  }

  @Test
  public void findWithinPeriodAndWithTransactionIdsTest_partitioningFiveTimes() {
    when(transactionRepository.findWithinPeriodAndWithTransactionIds(
      any(Instant.class),
      any(Instant.class),
      anySet()
      )
    ).thenReturn(getTransactions(10000));

    sut.findWithinPeriodAndWithTransactionIds(YearMonth.now(), getUUIDs(50000));

    verify(transactionRepository, times(5))
      .findWithinPeriodAndWithTransactionIds(any(), any(), anySet());
  }

  @Test
  public void findWithinPeriodAndWithTransactionIdsTest_partitioningOneTimes() {
    when(transactionRepository.findWithinPeriodAndWithTransactionIds(
      any(Instant.class),
      any(Instant.class),
      anySet()
      )
    ).thenReturn(getTransactions(1));

    sut.findWithinPeriodAndWithTransactionIds(YearMonth.now(), getUUIDs(1));

    verify(transactionRepository, times(1))
      .findWithinPeriodAndWithTransactionIds(any(), any(), anySet());
  }

  private Set<Transaction> getTransactions(int count) {

    Set<Transaction> transactions = new HashSet<>();

    for (int i = 0; i < count; i++) {

        final Member m = new Member();
      m.setId("124");

        final Transaction t = new Transaction();
      t.setId(UUID.randomUUID());
      t.setTransactionStatus(TransactionStatus.COMPLETED);
      t.setMember(m);
      t.setAmount(BigDecimal.TEN);
      t.setCurrency("SEK");
      t.setTimestamp(Instant.now());
      t.setTransactionType(TransactionType.CHARGE);

      transactions.add(t);
    }

    return transactions;
  }

  private Set<UUID> getUUIDs(int count) {
    Set<UUID> set = new HashSet<>();

    for (int i = 0; i < count; i++) {
      set.add(UUID.randomUUID());
    }

    return set;
  }

}
