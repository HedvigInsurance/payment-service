package com.hedvig.paymentservice.services.payments;

import com.hedvig.paymentservice.query.member.entities.Transaction;
import com.hedvig.paymentservice.query.member.entities.TransactionHistoryEvent;
import com.hedvig.paymentservice.query.member.entities.TransactionHistoryEventRepository;
import com.hedvig.paymentservice.query.member.entities.TransactionRepository;
import com.hedvig.paymentservice.services.exceptions.DuplicateTransactionHistoryEventException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class TransactionHistoryDao {
  private final TransactionHistoryEventRepository transactionHistoryEventRepository;
  private final TransactionRepository transactionRepository;

  @Autowired
  public TransactionHistoryDao(final TransactionHistoryEventRepository transactionHistoryEventRepository, final TransactionRepository transactionRepository) {
    this.transactionHistoryEventRepository = transactionHistoryEventRepository;
    this.transactionRepository = transactionRepository;
  }

  @Transactional
  public void add(final TransactionHistoryEvent transactionHistoryEvent) {
    final boolean thisTransactionHasEventAlready = StreamSupport.stream(transactionHistoryEventRepository.findAllForTransaction(transactionHistoryEvent.getTransactionId()).spliterator(), false)
      .map(TransactionHistoryEvent::getType)
      .anyMatch(type -> transactionHistoryEvent.getType().equals(type));

    if (thisTransactionHasEventAlready) {
      throw new DuplicateTransactionHistoryEventException(String.format(
        "Transaction %s already has an event of type %s",
        transactionHistoryEvent.getTransactionId(),
        transactionHistoryEvent.getType()));
    }

    transactionHistoryEventRepository.save(transactionHistoryEvent);
  }

  public Stream<TransactionHistoryEvent> findAllAsStream() {
    return StreamSupport.stream(transactionHistoryEventRepository.findAll().spliterator(), false);
  }

  public Stream<Transaction> findTransactionsAsStream(final Iterable<UUID> transactionIds) {
    return StreamSupport.stream(transactionRepository.findAllWithMemberByIds(transactionIds).spliterator(), false);
  }

  public void dangerouslyReset() {
    transactionHistoryEventRepository.deleteAll();
  }
}
