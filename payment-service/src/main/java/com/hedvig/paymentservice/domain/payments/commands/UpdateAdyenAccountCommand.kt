package com.hedvig.paymentservice.domain.payments.commands

import com.adyen.model.checkout.PaymentsResponse
import org.axonframework.commandhandling.TargetAggregateIdentifier

data class UpdateAdyenAccountCommand(
  @TargetAggregateIdentifier
  val memberId: String,
  val adyenTokenId: String,
  val recurringDetailReference: String?,
  val tokenStatus: PaymentsResponse.ResultCodeEnum
)
