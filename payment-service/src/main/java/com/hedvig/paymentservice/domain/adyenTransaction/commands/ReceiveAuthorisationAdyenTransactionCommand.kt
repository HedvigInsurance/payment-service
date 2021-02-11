package com.hedvig.paymentservice.domain.adyenTransaction.commands

import java.util.UUID
import javax.money.MonetaryAmount
import org.axonframework.commandhandling.TargetAggregateIdentifier

class ReceiveAuthorisationAdyenTransactionCommand(
    @TargetAggregateIdentifier
    val transactionId: UUID,
    val memberId: String,
    val amount: MonetaryAmount,
    val rescueReference: String?
)
