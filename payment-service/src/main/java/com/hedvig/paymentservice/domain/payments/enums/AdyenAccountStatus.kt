package com.hedvig.paymentservice.domain.payments.enums

import com.hedvig.paymentservice.domain.tokenRegistration.enums.TokenRegistrationStatus

enum class AdyenAccountStatus {
  AUTHORISED,
  PENDING,
  CANCELLED;

  companion object {
    fun fromTokenRegistrationStatus(s: TokenRegistrationStatus): AdyenAccountStatus {
      return when (s) {
        TokenRegistrationStatus.AUTHORISED -> AUTHORISED
        TokenRegistrationStatus.PENDING -> PENDING
        TokenRegistrationStatus.CANCELLED -> CANCELLED
      }
    }
  }
}
