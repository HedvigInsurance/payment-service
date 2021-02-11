package com.hedvig.paymentservice.domain.payments

import com.hedvig.paymentservice.domain.payments.enums.AdyenAccountStatus

data class AdyenPayoutAccount(
    val shopperReference: String,
    val status: AdyenAccountStatus
)
