package com.hedvig.paymentservice.domain.swish.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.UUID
import javax.money.MonetaryAmount

data class InitiateSwishTransactionPayoutCommand(
    @TargetAggregateIdentifier
    val transactionId: UUID,
    val memberId: String,
    val phoneNumber: String,
    val ssn: String,
    val message: String,
    val amount: MonetaryAmount,
)
