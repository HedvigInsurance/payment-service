package com.hedvig.paymentservice.domain.trustlyOrder.commands

import com.hedvig.paymentService.trustly.data.response.Error
import java.util.*
import org.axonframework.commandhandling.TargetAggregateIdentifier

data class PayoutErrorReceivedCommand(
    @TargetAggregateIdentifier  
    val hedvigOrderId: UUID,
    val error: Error
)
