package com.hedvig.paymentservice.query.member.entities;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionHistoryEventRepository extends CrudRepository<TransactionHistoryEvent, UUID> {
  @Query("SELECT the FROM TransactionHistoryEvent the WHERE the.transactionId = ?1")
  Iterable<TransactionHistoryEvent> findAllForTransaction(UUID transactionId);
}
