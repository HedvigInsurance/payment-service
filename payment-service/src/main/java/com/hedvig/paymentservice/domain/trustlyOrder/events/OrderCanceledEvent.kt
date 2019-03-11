package com.hedvig.paymentservice.domain.trustlyOrder.events

import java.util.UUID
import lombok.Value

data class OrderCanceledEvent (
    val hedvigOrderId: UUID
)
