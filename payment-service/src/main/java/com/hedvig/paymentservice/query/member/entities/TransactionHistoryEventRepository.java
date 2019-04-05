package com.hedvig.paymentservice.query.member.entities;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransactionHistoryEventRepository extends CrudRepository<TransactionHistoryEntity, UUID> {
  @Query("SELECT the FROM TransactionHistoryEntity the WHERE the.transactionId = ?1")
  Iterable<TransactionHistoryEntity> findAllForTransaction(UUID transactionId);
}
