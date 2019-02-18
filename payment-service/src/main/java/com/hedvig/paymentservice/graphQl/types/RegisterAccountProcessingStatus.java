package com.hedvig.paymentservice.graphQl.types;

public enum RegisterAccountProcessingStatus {
  NOT_INITIATED,
  INITIATED,
  REQUESTED,
  IN_PROGRESS,
  CONFIRMED,
  CANCELLED
}
