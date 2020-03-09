package com.hedvig.paymentservice.graphQl.types

enum class RegisterAccountProcessingStatus {
  NOT_INITIATED, INITIATED, REQUESTED, IN_PROGRESS, CONFIRMED, CANCELLED
}
