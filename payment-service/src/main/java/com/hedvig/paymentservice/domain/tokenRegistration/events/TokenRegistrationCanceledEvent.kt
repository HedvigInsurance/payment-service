package com.hedvig.paymentservice.domain.tokenRegistration.events

import com.hedvig.paymentservice.services.adyen.dtos.AdyenPaymentsResponse
import java.util.UUID

class TokenRegistrationCanceledEvent(
  val adyenTokenId: UUID,
  val memberId: String,
  val adyenPaymentsResponse: AdyenPaymentsResponse
)
