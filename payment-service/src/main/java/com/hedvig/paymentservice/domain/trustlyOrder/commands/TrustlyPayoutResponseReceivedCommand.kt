package com.hedvig.paymentservice.domain.trustlyOrder.commands

import java.util.*
import javax.money.MonetaryAmount
import org.axonframework.commandhandling.TargetAggregateIdentifier

data class TrustlyPayoutResponseReceivedCommand(
    @TargetAggregateIdentifier  
    val hedvigOrderId: UUID,
    val trustlyOrderId: String,
    val amount: MonetaryAmount
)
