package com.hedvig.paymentservice.graphQl.types

import com.hedvig.paymentservice.domain.payments.enums.AdyenAccountStatus

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

    fun fromAdyenAccountStatus(registrationStatus: AdyenAccountStatus): PayinMethodStatus {
      return when (registrationStatus) {
        AdyenAccountStatus.AUTHORISED -> ACTIVE
        AdyenAccountStatus.PENDING -> PENDING
        AdyenAccountStatus.CANCELLED -> NEEDS_SETUP
      }
    }
  }
}
