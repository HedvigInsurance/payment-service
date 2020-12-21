package com.hedvig.paymentservice.domain.adyenTransaction.events

import java.util.UUID

data class AdyenTransactionRetryUnsuccessfulResponseReceivedEvent(
    val transactionId: UUID,
    val memberId: String,
    val reason: String,
    val rescueReference: String,
    val orderAttemptNumber: Int?
)
