package com.hedvig.paymentservice.domain.trustlyOrder.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.*

data class SelectAccountResponseReceivedCommand(
  @TargetAggregateIdentifier
  val hedvigOrderId: UUID,
  val iframeUrl: String,
  val trustlyOrderId: String
)
