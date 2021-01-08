package com.hedvig.paymentservice.domain.adyenTransaction.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.UUID
import javax.money.MonetaryAmount

data class ReceivedDeclinedAdyenPayoutTransactionFromNotificationCommand(
    @TargetAggregateIdentifier
    val transactionId: UUID,
    val memberId: String,
    val amount: MonetaryAmount,
    val reason: String?
)
