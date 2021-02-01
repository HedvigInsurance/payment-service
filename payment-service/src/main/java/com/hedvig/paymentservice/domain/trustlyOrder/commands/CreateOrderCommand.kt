package com.hedvig.paymentservice.domain.trustlyOrder.commands

import java.util.UUID

data class CreateOrderCommand(
    val memberId: String,
    val hedvigOrderId: UUID
)
