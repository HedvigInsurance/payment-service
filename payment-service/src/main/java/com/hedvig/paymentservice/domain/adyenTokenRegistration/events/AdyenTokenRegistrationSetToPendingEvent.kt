package com.hedvig.paymentservice.domain.adyenTokenRegistration.events

import java.util.UUID

data class AdyenTokenRegistrationSetToPendingEvent(
    val adyenTokenRegistrationId: UUID,
    val memberId: String
)

