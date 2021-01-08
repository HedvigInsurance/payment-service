package com.hedvig.paymentservice.domain.adyenTransaction.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.UUID

class ReceiveCancellationResponseAdyenTransactionCommand(
    @TargetAggregateIdentifier
    val transactionId: UUID,
    val memberId: String,
    val reason: String
)
