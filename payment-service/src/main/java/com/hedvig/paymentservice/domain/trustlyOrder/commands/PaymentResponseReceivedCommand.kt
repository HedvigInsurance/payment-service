package com.hedvig.paymentservice.domain.trustlyOrder.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.*


data class PaymentResponseReceivedCommand(

  @TargetAggregateIdentifier
  val hedvigOrderId: UUID,
  val url: String?,
  val trustlyOrderId: String
)
