package com.hedvig.paymentservice.domain.swish.events

import java.util.UUID

class SwishPayoutTransactionCanceledEvent(
    val transactionId: UUID,
    val memberId: String,
    val reason: String,
    val httpStatusCode: Int?
)
