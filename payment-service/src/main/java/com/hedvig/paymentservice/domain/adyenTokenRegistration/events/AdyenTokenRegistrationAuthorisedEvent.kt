package com.hedvig.paymentservice.domain.adyenTokenRegistration.events

import com.hedvig.paymentservice.services.adyen.dtos.AdyenPaymentsResponse
import java.util.UUID
import org.axonframework.serialization.Revision

@Revision("2.0")
data class AdyenTokenRegistrationAuthorisedEvent(
    val adyenTokenRegistrationId: UUID,
    val memberId: String,
    val adyenPaymentsResponse: AdyenPaymentsResponse,
    val adyenMerchantAccount: String,
    val isPayoutSetup: Boolean,
    val shopperReference: String
)
