package com.hedvig.paymentservice.domain.adyenTokenRegistration.events

import com.hedvig.paymentservice.services.adyen.dtos.AdyenPaymentsResponse
import java.util.*

//Todo should I just use `AdyenTokenRegistrationAuthorisedEvent`
data class AdyenPayoutTokenRegistrationAuthorisedEvent(
    val adyenTokenRegistrationId: UUID,
    val memberId: String,
    val adyenPaymentsResponse: AdyenPaymentsResponse
)
