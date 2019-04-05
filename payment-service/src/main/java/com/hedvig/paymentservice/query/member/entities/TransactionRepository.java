package com.hedvig.paymentservice.query.member.entities;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, UUID> {
  @Query(
    "SELECT t FROM Transaction t " +
      "WHERE (t.timestamp BETWEEN :periodStart " +
      "AND :periodEnd) " +
      "AND t.id IN (:transactionIds)"
  )
  Set<Transaction> findWithinPeriodAndWithTransactionIds(
    @Param("periodStart") Instant periodStart,
    @Param("periodEnd") Instant periodEnd,
    @Param("transactionIds") Iterable <UUID> transactionIds
  );
}
