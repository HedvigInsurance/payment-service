package com.hedvig.paymentservice.domain.trustlyOrder.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.*
import javax.money.MonetaryAmount

data class AdyenPayoutResponseReceivedCommand(
  @TargetAggregateIdentifier
  val hedvigOrderId: UUID,
  val orderId: String,
  val amount: MonetaryAmount
)
