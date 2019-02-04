package com.hedvig.paymentservice.domain.accountRegistration.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.*

data class ReceiveAccountRegistrationResponseCommand(
  @TargetAggregateIdentifier
  val accountRegistrationId: UUID
)
