package com.hedvig.paymentservice.domain.trustlyOrder.commands

import java.util.*
import org.axonframework.commandhandling.TargetAggregateIdentifier

data class PaymentResponseReceivedCommand(

    @TargetAggregateIdentifier  
    val hedvigOrderId: UUID,
    val url: String?,
    val trustlyOrderId: String
)
