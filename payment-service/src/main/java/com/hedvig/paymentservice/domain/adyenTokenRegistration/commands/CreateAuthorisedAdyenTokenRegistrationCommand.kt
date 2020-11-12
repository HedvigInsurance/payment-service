package com.hedvig.paymentservice.domain.adyenTokenRegistration.commands

import com.hedvig.paymentservice.services.adyen.dtos.AdyenMerchantInfo
import com.hedvig.paymentservice.services.adyen.dtos.AdyenPaymentsResponse
import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.UUID

data class CreateAuthorisedAdyenTokenRegistrationCommand(
  @TargetAggregateIdentifier
  val adyenTokenRegistrationId: UUID,
  val memberId: String,
  val adyenMerchantInfo: AdyenMerchantInfo,
  val adyenPaymentsResponse: AdyenPaymentsResponse,
  val isPayoutSetup: Boolean = false,
  val shopperReference: String
)
