package com.hedvig.paymentservice.domain.adyen.events

import com.hedvig.paymentservice.services.adyen.dtos.AdyenPaymentsResponse
import java.util.UUID

data class AdyenTokenCreatedEvent(
  val adyenTokenId: UUID,
  val memberId: String,
  val tokenizationResponse: AdyenPaymentsResponse
)
