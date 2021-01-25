package com.hedvig.paymentservice.graphQl.types

import com.hedvig.paymentservice.domain.adyenTokenRegistration.enums.AdyenTokenRegistrationStatus

enum class PayoutMethodStatus {
    ACTIVE, PENDING, NEEDS_SETUP;

    companion object {
        fun from(adyenTokenRegistrationStatus: AdyenTokenRegistrationStatus): PayoutMethodStatus {
            return when (adyenTokenRegistrationStatus) {
                AdyenTokenRegistrationStatus.AUTHORISED -> ACTIVE
                AdyenTokenRegistrationStatus.PENDING -> PENDING
                AdyenTokenRegistrationStatus.CANCELLED -> NEEDS_SETUP
            }
        }
    }
}
