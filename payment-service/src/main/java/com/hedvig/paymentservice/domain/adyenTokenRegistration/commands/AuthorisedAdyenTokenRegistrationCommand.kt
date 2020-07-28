package com.hedvig.paymentservice.domain.adyenTokenRegistration.commands

import com.hedvig.paymentservice.services.adyen.dtos.AdyenPaymentsResponse
import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.UUID

class AuthorisedAdyenTokenRegistrationCommand(
  @TargetAggregateIdentifier
  val adyenTokenRegistrationId: UUID,
  val memberId: String,
  val adyenPaymentsResponse: AdyenPaymentsResponse,
  val shopperReference: String
)
