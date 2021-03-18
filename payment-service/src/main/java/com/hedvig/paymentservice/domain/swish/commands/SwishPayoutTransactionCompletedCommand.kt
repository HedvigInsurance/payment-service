package com.hedvig.paymentservice.domain.swish.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.UUID

class SwishPayoutTransactionCompletedCommand(
    @TargetAggregateIdentifier
    val transactionId: UUID,
    val memberId: String
)
