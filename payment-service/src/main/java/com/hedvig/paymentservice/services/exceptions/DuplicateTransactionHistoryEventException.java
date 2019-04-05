package com.hedvig.paymentservice.services.exceptions;

public class DuplicateTransactionHistoryEventException extends RuntimeException {
  public DuplicateTransactionHistoryEventException(final String message) {
    super(message);
  }
}
