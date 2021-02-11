package com.hedvig.paymentservice.domain.payments.events

import com.hedvig.paymentservice.domain.payments.enums.AdyenAccountStatus

class AdyenAccountUpdatedEvent(
    val memberId: String,
    val recurringDetailReference: String,
    val accountStatus: AdyenAccountStatus
)
