package com.hedvig.paymentservice.services.payments;

import com.hedvig.paymentservice.query.member.entities.Transaction;
import com.hedvig.paymentservice.query.member.entities.TransactionHistoryEntity;
import com.hedvig.paymentservice.query.member.entities.TransactionHistoryEventRepository;
import com.hedvig.paymentservice.query.member.entities.TransactionRepository;
import com.hedvig.paymentservice.services.exceptions.DuplicateTransactionHistoryEventException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Set;
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
  public void add(final TransactionHistoryEntity transactionHistoryEntity) {
    final boolean thisTransactionHasEventAlready = StreamSupport.stream(transactionHistoryEventRepository.findAllForTransaction(transactionHistoryEntity.getTransactionId()).spliterator(), false)
      .map(TransactionHistoryEntity::getType)
      .anyMatch(type -> transactionHistoryEntity.getType().equals(type));

    if (thisTransactionHasEventAlready) {
      throw new DuplicateTransactionHistoryEventException(String.format(
        "Transaction %s already has an event of type %s",
        transactionHistoryEntity.getTransactionId(),
        transactionHistoryEntity.getType()));
    }

    transactionHistoryEventRepository.save(transactionHistoryEntity);
  }

  public Stream<TransactionHistoryEntity> findAllAsStream() {
    return StreamSupport.stream(transactionHistoryEventRepository.findAll().spliterator(), false);
  }

  public Set<Transaction> findWithinPeriodAndWithTransactionIds(final YearMonth period, final Set<UUID> transactionIds) {
    final Instant periodStart = period.atDay(1)
      .atStartOfDay()
      .atZone(ZoneId.of("Europe/Stockholm"))
      .toInstant();
    final Instant periodEnd = period.atEndOfMonth()
      .atTime(23, 59, 59, 999_999_999)
      .atZone(ZoneId.of("Europe/Stockholm"))
      .toInstant();
    return transactionRepository
      .findWithinPeriodAndWithTransactionIds(periodStart, periodEnd, transactionIds);
  }

  public void dangerouslyReset() {
    transactionHistoryEventRepository.deleteAll();
  }
}
