package com.hedvig.paymentservice.domain.trustlyOrder.commands

import java.util.UUID
import lombok.Value

//@Value
data class CreateOrderCommand(
    val memberId: String,
    val hedvigOrderId: UUID
)
