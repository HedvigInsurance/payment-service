package com.hedvig.paymentservice.domain.adyenTokenRegistration.events

import com.hedvig.paymentservice.services.adyen.dtos.AdyenPaymentsResponse
import org.axonframework.serialization.Revision
import java.util.UUID

@Revision("2.0")
class PendingAdyenTokenRegistrationCreatedEvent(
  val adyenTokenRegistrationId: UUID,
  val memberId: String,
  val adyenPaymentsResponse: AdyenPaymentsResponse,
  val paymentDataFromAction: String,
  val isPayoutSetup: Boolean
)
