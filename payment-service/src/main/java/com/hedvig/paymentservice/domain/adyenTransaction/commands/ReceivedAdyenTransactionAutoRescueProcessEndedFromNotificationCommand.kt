package com.hedvig.paymentservice.domain.adyenTransaction.commands

import java.util.UUID
import org.axonframework.commandhandling.TargetAggregateIdentifier
import org.javamoney.moneta.Money

data class ReceivedAdyenTransactionAutoRescueProcessEndedFromNotificationCommand(
    @TargetAggregateIdentifier
    val transactionId: UUID,
    val memberId: String,
    val amount: Money,
    val reason: String,
    val rescueReference: String,
    val retryWasSuccessful: Boolean,
    val orderAttemptNumber: Int?
)
