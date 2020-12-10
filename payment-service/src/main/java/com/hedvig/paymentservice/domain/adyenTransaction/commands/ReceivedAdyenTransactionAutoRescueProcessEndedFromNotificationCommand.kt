package com.hedvig.paymentservice.domain.adyenTransaction.commands

import org.javamoney.moneta.Money
import java.util.UUID

data class ReceivedAdyenTransactionAutoRescueProcessEndedFromNotificationCommand(
    val transactionId: UUID,
    val memberId: String,
    val amount: Money,
    val reason: String,
    val rescueReference: String,
    val retryWasSuccessful: Boolean
)
