package com.hedvig.paymentservice.domain.adyenTransaction.commands

import java.util.UUID
import javax.money.MonetaryAmount
import org.axonframework.commandhandling.TargetAggregateIdentifier

data class InitiateAdyenTransactionCommand(
    @TargetAggregateIdentifier
    val transactionId: UUID,
    val memberId: String,
    val recurringDetailReference: String,
    val amount: MonetaryAmount
)
