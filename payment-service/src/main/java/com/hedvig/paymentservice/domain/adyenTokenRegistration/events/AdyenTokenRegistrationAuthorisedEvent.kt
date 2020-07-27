package com.hedvig.paymentservice.domain.adyenTokenRegistration.events

import com.hedvig.paymentservice.services.adyen.dtos.AdyenPaymentsResponse
import org.axonframework.serialization.Revision
import java.util.UUID

@Revision("2.0")
data class AdyenTokenRegistrationAuthorisedEvent(
  val adyenTokenRegistrationId: UUID,
  val memberId: String,
  val adyenPaymentsResponse: AdyenPaymentsResponse,
  val isPayoutSetup: Boolean
)
