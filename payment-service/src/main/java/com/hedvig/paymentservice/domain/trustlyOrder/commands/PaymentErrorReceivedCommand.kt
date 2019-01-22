package com.hedvig.paymentservice.domain.trustlyOrder.commands

import com.hedvig.paymentService.trustly.data.response.Error
import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.*

data class PaymentErrorReceivedCommand(
  @TargetAggregateIdentifier
  val hedvigOrderId: UUID,
  val error: Error
)
