package com.hedvig.paymentservice.domain.adyenTransaction.commands

import java.util.UUID
import javax.money.MonetaryAmount
import org.axonframework.commandhandling.TargetAggregateIdentifier

data class ReceivedSuccessfulAdyenPayoutTransactionFromNotificationCommand(
    @TargetAggregateIdentifier
    val transactionId: UUID,
    val memberId: String,
    val amount: MonetaryAmount
)
