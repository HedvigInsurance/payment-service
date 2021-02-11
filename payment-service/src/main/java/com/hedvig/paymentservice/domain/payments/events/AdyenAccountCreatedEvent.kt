package com.hedvig.paymentservice.domain.payments.events

import com.hedvig.paymentservice.domain.payments.enums.AdyenAccountStatus

data class AdyenAccountCreatedEvent(
    val memberId: String,
    val recurringDetailReference: String,
    val accountStatus: AdyenAccountStatus
)
