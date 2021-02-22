package com.hedvig.paymentservice.domain.payments.commands

import java.util.UUID
import org.axonframework.commandhandling.TargetAggregateIdentifier

data class ChargeFailedCommand(
    @TargetAggregateIdentifier  
    val memberId: String,
    val transactionId: UUID
)
