package com.hedvig.paymentservice.domain.adyenTransaction.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.UUID

class ReceiveAdyenTransactionUnsuccessfulRetryResponseCommand(
    @TargetAggregateIdentifier
    val transactionId: UUID,
    val memberId: String,
    val reason: String,
    val rescueReference: String,
    val orderAttemptNumber: Int
)
