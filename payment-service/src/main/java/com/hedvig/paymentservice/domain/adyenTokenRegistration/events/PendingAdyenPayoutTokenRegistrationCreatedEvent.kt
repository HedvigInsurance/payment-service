package com.hedvig.paymentservice.domain.adyenTokenRegistration.events

import com.hedvig.paymentservice.services.adyen.dtos.AdyenPaymentsResponse
import java.util.*

//Todo should I just use `PendingAdyenTokenRegistrationCreatedEvent`
class PendingAdyenPayoutTokenRegistrationCreatedEvent(
    val adyenTokenRegistrationId: UUID,
    val memberId: String,
    val adyenPaymentsResponse: AdyenPaymentsResponse,
    val paymentDataFromAction: String
)
