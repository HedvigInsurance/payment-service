package com.hedvig.paymentservice.graphQl.types

enum class PayinMethodStatus {
  ACTIVE, PENDING, NEEDS_SETUP;


  companion object {
    fun fromTrustlyDirectDebitStatus(status: DirectDebitStatus): PayinMethodStatus {
      return when (status) {
        DirectDebitStatus.ACTIVE -> ACTIVE
        DirectDebitStatus.NEEDS_SETUP -> NEEDS_SETUP
        DirectDebitStatus.PENDING -> PENDING
      }
    }
  }
}
