package com.hedvig.paymentservice.domain.accountRegistration.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.*

data class ReceiveAccountRegistrationCancellationCommand(
  @TargetAggregateIdentifier
  val accountRegistrationId: UUID,

  val hedvigOrderId: UUID,
  val memberId: String
)
