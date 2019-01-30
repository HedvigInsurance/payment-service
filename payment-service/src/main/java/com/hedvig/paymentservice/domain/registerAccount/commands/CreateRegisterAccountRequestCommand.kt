package com.hedvig.paymentservice.domain.registerAccount.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.*

data class CreateRegisterAccountRequestCommand(
  @TargetAggregateIdentifier
  val hedvigOrderId: UUID,
  val memberId: String
)
