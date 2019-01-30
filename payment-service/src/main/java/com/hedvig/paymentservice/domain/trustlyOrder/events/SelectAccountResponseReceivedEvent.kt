package com.hedvig.paymentservice.domain.trustlyOrder.events

import java.util.UUID

import lombok.Data
import lombok.Value

@Value
class SelectAccountResponseReceivedEvent {

    val hedvigOrderId: UUID? = null
    val iframeUrl: String? = null
}
