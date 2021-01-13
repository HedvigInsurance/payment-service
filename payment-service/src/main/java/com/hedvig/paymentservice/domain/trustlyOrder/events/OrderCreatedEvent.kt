package com.hedvig.paymentservice.domain.trustlyOrder.events

import java.util.UUID

data class OrderCreatedEvent(
    val hedvigOrderId: UUID,
    val memberId: String
)
