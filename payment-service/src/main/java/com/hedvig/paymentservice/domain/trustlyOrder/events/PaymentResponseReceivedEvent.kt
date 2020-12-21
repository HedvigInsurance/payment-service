package com.hedvig.paymentservice.domain.trustlyOrder.events

import java.util.*

data class PaymentResponseReceivedEvent(
    val hedvigOrderId: UUID,
    val url: String?
)
