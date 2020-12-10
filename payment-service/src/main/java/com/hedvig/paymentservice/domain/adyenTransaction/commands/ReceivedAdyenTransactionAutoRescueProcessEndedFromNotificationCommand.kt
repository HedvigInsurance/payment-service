package com.hedvig.paymentservice.domain.adyenTransaction.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier
import org.javamoney.moneta.Money
import java.util.UUID

data class ReceivedAdyenTransactionAutoRescueProcessEndedFromNotificationCommand(
    @TargetAggregateIdentifier
    val transactionId: UUID,
    val memberId: String,
    val amount: Money,
    val reason: String,
    val rescueReference: String,
    val retryWasSuccessful: Boolean
)
