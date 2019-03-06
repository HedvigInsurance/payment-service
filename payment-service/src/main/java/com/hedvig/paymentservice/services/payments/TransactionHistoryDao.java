package com.hedvig.paymentservice.services.payments;

import com.hedvig.paymentservice.query.member.entities.TransactionHistoryEvent;
import com.hedvig.paymentservice.query.member.entities.TransactionHistoryEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class TransactionHistoryDao {
  private final TransactionHistoryEventRepository transactionHistoryEventRepository;

  @Autowired
  public TransactionHistoryDao(final TransactionHistoryEventRepository transactionHistoryEventRepository) {
    this.transactionHistoryEventRepository = transactionHistoryEventRepository;
  }

  public void add(final TransactionHistoryEvent transactionHistoryEvent) {
    transactionHistoryEventRepository.save(transactionHistoryEvent);
  }

  public Stream<TransactionHistoryEvent> findAllAsStream() {
    return StreamSupport.stream(transactionHistoryEventRepository.findAll().spliterator(), false);
  }
}
