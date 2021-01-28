package com.hedvig.paymentservice.graphQl.types

import com.hedvig.paymentservice.domain.adyenTokenRegistration.enums.AdyenTokenRegistrationStatus
import com.hedvig.paymentservice.services.adyen.dtos.PaymentResponseResultCode

enum class PayoutMethodStatus {
    ACTIVE,
    PENDING,
    NEEDS_SETUP;

    companion object {
        fun from(status: AdyenTokenRegistrationStatus): PayoutMethodStatus = when (status) {
            AdyenTokenRegistrationStatus.AUTHORISED -> ACTIVE
            AdyenTokenRegistrationStatus.PENDING -> PENDING
            AdyenTokenRegistrationStatus.CANCELLED -> NEEDS_SETUP
        }

        fun from(resultCode: PaymentResponseResultCode): PayoutMethodStatus = when (resultCode) {
            PaymentResponseResultCode.AUTHORISED -> PayoutMethodStatus.ACTIVE
            PaymentResponseResultCode.PENDING -> PayoutMethodStatus.PENDING
            PaymentResponseResultCode.FAILED -> PayoutMethodStatus.NEEDS_SETUP
        }
    }
}
