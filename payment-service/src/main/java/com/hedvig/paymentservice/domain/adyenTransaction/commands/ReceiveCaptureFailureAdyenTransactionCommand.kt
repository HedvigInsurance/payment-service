package com.hedvig.paymentservice.domain.adyenTransaction.commands

import java.util.UUID
import org.axonframework.commandhandling.TargetAggregateIdentifier

data class ReceiveCaptureFailureAdyenTransactionCommand(
    @TargetAggregateIdentifier
    val transactionId: UUID,
    val memberId: String
)
