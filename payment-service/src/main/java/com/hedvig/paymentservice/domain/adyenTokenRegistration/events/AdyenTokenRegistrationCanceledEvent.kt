package com.hedvig.paymentservice.domain.adyenTokenRegistration.events

import com.hedvig.paymentservice.services.adyen.dtos.AdyenPaymentsResponse
import java.util.UUID

class AdyenTokenRegistrationCanceledEvent(
    val adyenTokenRegistrationId: UUID,
    val memberId: String,
    val adyenPaymentsResponse: AdyenPaymentsResponse
)
