package com.hedvig.paymentservice.domain.adyenTokenRegistration.events

import java.util.UUID

data class AdyenTokenRegistrationCanceledFromNotificationEvent(
    val adyenTokenRegistrationId: UUID,
    val memberId: String
)
