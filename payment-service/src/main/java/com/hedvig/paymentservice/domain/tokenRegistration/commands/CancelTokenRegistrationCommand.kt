package com.hedvig.paymentservice.domain.tokenRegistration.commands

import com.hedvig.paymentservice.services.adyen.dtos.AdyenPaymentsResponse
import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.UUID

class CancelTokenRegistrationCommand(
  @TargetAggregateIdentifier
  val tokenRegistrationId: UUID,
  val memberId: String,
  val adyenPaymentsResponse: AdyenPaymentsResponse
)
