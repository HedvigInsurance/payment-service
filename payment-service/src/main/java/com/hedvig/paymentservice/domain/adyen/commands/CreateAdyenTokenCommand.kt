package com.hedvig.paymentservice.domain.adyen.commands

import com.hedvig.paymentservice.graphQl.types.TokenizationResponse
import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.UUID

data class CreateAdyenTokenCommand(
  @TargetAggregateIdentifier
  val adyenTokenId: UUID,
  val memberId: String,
  val tokenizationResponse: TokenizationResponse
)
