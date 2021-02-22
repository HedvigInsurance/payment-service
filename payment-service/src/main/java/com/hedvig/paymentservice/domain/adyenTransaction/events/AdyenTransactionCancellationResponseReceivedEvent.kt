package com.hedvig.paymentservice.domain.adyenTransaction.events

import java.util.UUID

class AdyenTransactionCancellationResponseReceivedEvent(
    val transactionId: UUID,
    val memberId: String,
    val reason: String
)
