package com.hedvig.paymentservice.domain.payments.transactionhistory;

import com.hedvig.paymentservice.domain.payments.events.*;
import com.hedvig.paymentservice.query.member.entities.TransactionHistoryEvent;
import com.hedvig.paymentservice.query.member.entities.TransactionHistoryEventType;
import com.hedvig.paymentservice.services.payments.TransactionHistoryDao;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.axonframework.eventhandling.Timestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@ProcessingGroup("TransactionHistory")
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
        null)
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
        null)
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
        null)
    );
  }

  @EventHandler
  public void on(final ChargeFailedEvent e, @Timestamp final Instant time) {
    transactionHistoryDao.add(
      new TransactionHistoryEvent(
        e.getTransactionId(),
        time,
        TransactionHistoryEventType.FAILED
      )
    );
  }

  @EventHandler
  public void on(final ChargeErroredEvent e) {
    transactionHistoryDao.add(
      new TransactionHistoryEvent(
        e.getTransactionId(),
        e.getAmount().getNumber().numberValueExact(BigDecimal.class),
        e.getAmount().getCurrency().getCurrencyCode(),
        e.getTimestamp(),
        TransactionHistoryEventType.ERROR,
        e.getReason()
      )
    );
  }

  @EventHandler
  public void on(final PayoutCompletedEvent e) {
    transactionHistoryDao.add(
      new TransactionHistoryEvent(
        e.getTransactionId(),
        e.getTimestamp(),
        TransactionHistoryEventType.COMPLETED)
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
        null)
    );
  }

  @EventHandler
  public void on(final PayoutErroredEvent e) {
    transactionHistoryDao.add(
      new TransactionHistoryEvent(
        e.getTransactionId(),
        e.getAmount().getNumber().numberValueExact(BigDecimal.class),
        e.getAmount().getCurrency().getCurrencyCode(),
        e.getTimestamp(),
        TransactionHistoryEventType.ERROR,
        e.getReason()
      )
    );
  }

  @ResetHandler
  public void handleReset() {
    transactionHistoryDao.dangerouslyReset();
  }
}
