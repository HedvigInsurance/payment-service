package com.hedvig.paymentservice.domain.payments.events

import com.hedvig.paymentservice.domain.payments.enums.AdyenAccountStatus

class AdyenPayoutAccountUpdatedEvent(
    val memberId: String,
    val shopperReference: String,
    val accountStatus: AdyenAccountStatus
)
