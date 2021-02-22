package com.hedvig.paymentservice.domain.adyenTransaction.commands

import java.util.UUID
import org.axonframework.commandhandling.TargetAggregateIdentifier

data class ReceiveAdyenTransactionUnsuccessfulRetryResponseCommand(
    @TargetAggregateIdentifier
    val transactionId: UUID,
    val memberId: String,
    val reason: String,
    val rescueReference: String,
    val orderAttemptNumber: Int?
)
