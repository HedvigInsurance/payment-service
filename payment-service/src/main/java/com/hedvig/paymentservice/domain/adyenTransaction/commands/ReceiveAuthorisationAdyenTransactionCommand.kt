package com.hedvig.paymentservice.domain.adyenTransaction.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier
import org.axonframework.serialization.Revision
import java.util.UUID

@Revision("1.0")
class ReceiveAuthorisationAdyenTransactionCommand(
    @TargetAggregateIdentifier
    val transactionId: UUID,
    val memberId: String,
    val rescueReference: String?
)
