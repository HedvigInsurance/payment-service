package com.hedvig.paymentservice.domain.trustlyOrder.events

import java.util.UUID

data class ExternalTransactionIdAssignedEvent(
    val hedvigOrderId: UUID,
    val transactionId: UUID,
    val memberId: String,
)
