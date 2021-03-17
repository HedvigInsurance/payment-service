package com.hedvig.paymentservice.domain.swish.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.UUID

class SwishPayoutTransactionFailedCommand(
    @TargetAggregateIdentifier
    val transactionId: UUID,
    val memberId: String,
    val errorCode :String?,
    val errorMessage :String?,
    val additionalInformation :String?
)
