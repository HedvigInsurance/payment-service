package com.hedvig.paymentservice.domain.payments.backfill;

import com.hedvig.paymentservice.domain.payments.events.*;
import com.hedvig.paymentservice.query.member.entities.Transaction;
import com.hedvig.paymentservice.query.member.entities.TransactionHistoryEvent;
import com.hedvig.paymentservice.query.member.entities.TransactionHistoryEventType;
import com.hedvig.paymentservice.services.payments.TransactionHistoryDao;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

import static java.util.Collections.singletonList;

@Service
public class TransactionHistoryEventListener {
  private TransactionHistoryDao transactionHistoryDao;

  @Autowired
  public TransactionHistoryEventListener(final TransactionHistoryDao transactionHistoryDao) {
    this.transactionHistoryDao = transactionHistoryDao;
  }

  @EventHandler
  public void on(final ChargeCreatedEvent e) {
    transactionHistoryDao.add(
      new TransactionHistoryEvent(
        e.getTransactionId(),
        e.getAmount().getNumber().numberValueExact(BigDecimal.class),
        e.getAmount().getCurrency().getCurrencyCode(),
        e.getTimestamp(),
        TransactionHistoryEventType.CREATED,
        null),
      true
    );
  }

  @EventHandler
  public void on(final PayoutCreatedEvent e) {
    transactionHistoryDao.add(
      new TransactionHistoryEvent(
        e.getTransactionId(),
        e.getAmount().getNumber().numberValueExact(BigDecimal.class),
        e.getAmount().getCurrency().getCurrencyCode(),
        e.getTimestamp(),
        TransactionHistoryEventType.CREATED,
        null),
      true
    );
  }

  @EventHandler
  public void on(final ChargeCompletedEvent e) {
    transactionHistoryDao.add(
      new TransactionHistoryEvent(
        e.getTransactionId(),
        e.getAmount().getNumber().numberValueExact(BigDecimal.class),
        e.getAmount().getCurrency().getCurrencyCode(),
        e.getTimestamp(),
        TransactionHistoryEventType.COMPLETED,
        null),
      true
    );
  }

  @EventHandler
  public void on(final ChargeFailedEvent e) {
    final Transaction tx = transactionHistoryDao.findTransactionsAsStream(singletonList(e.getTransactionId()))
      .findFirst()
      .get();
    transactionHistoryDao.add(
      new TransactionHistoryEvent(
        e.getTransactionId(),
        tx.getMoney().getNumber().numberValueExact(BigDecimal.class),
        tx.getMoney().getCurrency().getCurrencyCode(),
        Instant.now(),
        TransactionHistoryEventType.COMPLETED,
        null),
      true
    );
  }

  @EventHandler
  public void on(final PayoutCompletedEvent e) {
    transactionHistoryDao.add(
      new TransactionHistoryEvent(
        e.getTransactionId(),
        e.getTimestamp(),
        TransactionHistoryEventType.COMPLETED),
      true
    );
  }

  @EventHandler
  public void on(final PayoutFailedEvent e) {
    transactionHistoryDao.add(
      new TransactionHistoryEvent(
        e.getTransactionId(),
        e.getAmount().getNumber().numberValueExact(BigDecimal.class),
        e.getAmount().getCurrency().getCurrencyCode(),
        e.getTimestamp(),
        TransactionHistoryEventType.FAILED,
        null),
      true
    );
  }

  @ResetHandler
  public void handleReset() {
    transactionHistoryDao.dangerouslyReset();
  }
}
