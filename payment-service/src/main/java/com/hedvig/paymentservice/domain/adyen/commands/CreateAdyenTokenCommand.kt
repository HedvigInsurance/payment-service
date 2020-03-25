package com.hedvig.paymentservice.domain.adyen.commands

import com.hedvig.paymentservice.services.adyen.dtos.AdyenPaymentsResponse
import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.UUID

data class CreateAdyenTokenCommand(
  @TargetAggregateIdentifier
  val adyenTokenId: UUID,
  val memberId: String,
  val tokenizationResponse: AdyenPaymentsResponse
)
