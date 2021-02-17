package com.hedvig.paymentservice.domain.swish.events

import java.util.UUID

class SwishPayoutTransactionCompletedEvent(
    val transactionId: UUID,
    val memberId: String
)
