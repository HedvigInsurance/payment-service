package com.hedvig.paymentservice.domain.registerAccount.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.*

data class ReceiveRegisterAccountConfirmationCommand(
  @TargetAggregateIdentifier
  val hedvigOrderId: UUID,
  val memberId: String
)
