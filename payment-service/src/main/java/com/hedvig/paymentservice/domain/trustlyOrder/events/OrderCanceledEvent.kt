package com.hedvig.paymentservice.domain.trustlyOrder.events

import java.util.UUID

data class OrderCanceledEvent(
    val hedvigOrderId: UUID
)
