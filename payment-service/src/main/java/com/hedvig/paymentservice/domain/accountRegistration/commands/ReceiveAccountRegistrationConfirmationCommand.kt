package com.hedvig.paymentservice.domain.accountRegistration.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.*

data class ReceiveAccountRegistrationConfirmationCommand(
  @TargetAggregateIdentifier
  val accountRegistrationId: UUID,
  val memberId: String
)
