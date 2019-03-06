package com.hedvig.paymentservice.query.member.entities;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransactionHistoryEventRepository extends CrudRepository<TransactionHistoryEvent, UUID> {
}
