package com.hedvig.paymentservice.domain.adyen.events

import com.hedvig.paymentservice.graphQl.types.TokenizationResponse
import java.util.UUID

data class AdyenTokenCreatedEvent(
  val adyenTokenId: UUID,
  val memberId: String,
  val tokenizationResponse: TokenizationResponse
)
