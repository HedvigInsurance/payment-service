package com.hedvig.paymentservice.services.exceptions;

import com.hedvig.paymentservice.query.member.entities.TransactionHistoryEventType;

import java.util.UUID;

public class DuplicateTransactionHistoryEventException extends RuntimeException {
  public DuplicateTransactionHistoryEventException(final String message) {
    super(message);
  }

  public static DuplicateTransactionHistoryEventException from(
    final UUID transactionId,
    final TransactionHistoryEventType transactionType
  ) {
    return new DuplicateTransactionHistoryEventException(
      String.format("Transaction %s already has an event of type %s", transactionId, transactionType));
  }
}
