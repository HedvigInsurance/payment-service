package com.hedvig.paymentservice.domain.adyenTransaction.commands

import java.util.UUID
import org.axonframework.commandhandling.TargetAggregateIdentifier

class ReceiveCancellationResponseAdyenTransactionCommand(
    @TargetAggregateIdentifier
    val transactionId: UUID,
    val memberId: String,
    val reason: String
)
