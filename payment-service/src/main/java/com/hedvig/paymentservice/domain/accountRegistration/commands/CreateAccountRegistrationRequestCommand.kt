package com.hedvig.paymentservice.domain.accountRegistration.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.*

data class CreateAccountRegistrationRequestCommand(
  @TargetAggregateIdentifier
  val accountRegistrationId: UUID,

  val hedvigOrderId: UUID,
  val memberId: String,
  val trustlyOrderId: String,
  val trustlyUrl: String
)
