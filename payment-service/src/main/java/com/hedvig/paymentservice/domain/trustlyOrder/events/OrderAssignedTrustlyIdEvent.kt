package com.hedvig.paymentservice.domain.trustlyOrder.events

import java.util.UUID

data class OrderAssignedTrustlyIdEvent(
    val hedvigOrderId: UUID,
    val trustlyOrderId: String
)
