package com.hedvig.paymentservice.domain.trustlyOrder.commands

import java.util.*
import org.axonframework.commandhandling.TargetAggregateIdentifier

data class SelectAccountResponseReceivedCommand(
    @TargetAggregateIdentifier  
    val hedvigOrderId: UUID,
    val iframeUrl: String,
    val trustlyOrderId: String
)
