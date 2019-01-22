package com.hedvig.paymentservice.domain.trustlyOrder.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.*
import javax.money.MonetaryAmount

data class PayoutResponseReceivedCommand(
  @TargetAggregateIdentifier
  val hedvigOrderId: UUID,
  val trustlyOrderId: String,
  val amount: MonetaryAmount
)
