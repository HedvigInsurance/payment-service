package com.hedvig.paymentservice.domain.payments.commands

import java.time.Instant
import java.util.UUID
import javax.money.MonetaryAmount
import org.axonframework.commandhandling.TargetAggregateIdentifier

data class PayoutFailedCommand(
    @TargetAggregateIdentifier  
    val memberId: String,
    val transactionId: UUID,
    val amount: MonetaryAmount,
    val timestamp: Instant
)
