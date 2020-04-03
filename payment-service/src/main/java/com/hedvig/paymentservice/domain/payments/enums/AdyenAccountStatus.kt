package com.hedvig.paymentservice.domain.payments.enums

import com.hedvig.paymentservice.domain.adyenTokenRegistration.enums.AdyenTokenRegistrationStatus

enum class AdyenAccountStatus {
  AUTHORISED,
  PENDING,
  CANCELLED;

  companion object {
    fun fromTokenRegistrationStatus(s: AdyenTokenRegistrationStatus): AdyenAccountStatus {
      return when (s) {
        AdyenTokenRegistrationStatus.AUTHORISED -> AUTHORISED
        AdyenTokenRegistrationStatus.PENDING -> PENDING
        AdyenTokenRegistrationStatus.CANCELLED -> CANCELLED
      }
    }
  }
}
