package com.hedvig.paymentservice.domain.payments.commands

import com.hedvig.paymentservice.domain.tokenRegistration.enums.TokenRegistrationStatus
import org.axonframework.commandhandling.TargetAggregateIdentifier

data class UpdateAdyenAccountCommand(
  @TargetAggregateIdentifier
  val memberId: String,
  val recurringDetailReference: String,
  val tokenStatus: TokenRegistrationStatus
)
