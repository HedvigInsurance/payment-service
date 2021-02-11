package com.hedvig.paymentservice.domain.adyenTransaction.events

import java.util.UUID

class CaptureFailureAdyenTransactionReceivedEvent(
    val transactionId: UUID,
    val memberId: String
)
