package com.hedvig.paymentservice.domain.swish.events

import java.util.UUID

class SwishPayoutTransactionConfirmedEvent(
    val transactionId: UUID,
    val memberId: String
)
