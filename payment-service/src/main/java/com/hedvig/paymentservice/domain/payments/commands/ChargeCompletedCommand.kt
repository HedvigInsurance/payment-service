package com.hedvig.paymentservice.domain.payments.commands

import java.time.Instant
import java.util.UUID
import javax.money.MonetaryAmount
import org.axonframework.commandhandling.TargetAggregateIdentifier

data class ChargeCompletedCommand(
    @TargetAggregateIdentifier  
    var memberId: String,
    var transactionId: UUID,
    var amount: MonetaryAmount,
    var timestamp: Instant
)
