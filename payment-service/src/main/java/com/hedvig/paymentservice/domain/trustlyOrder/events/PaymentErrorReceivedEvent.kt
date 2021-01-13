package com.hedvig.paymentservice.domain.trustlyOrder.events

import com.hedvig.paymentService.trustly.data.response.Error
import java.util.*

data class PaymentErrorReceivedEvent(
    val hedvigOrderId: UUID,
    val error: Error,
)
