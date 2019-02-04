package com.hedvig.paymentservice.domain.trustlyOrder.events

import java.util.*

data class SelectAccountResponseReceivedEvent(
  val hedvigOrderId: UUID,
  val iframeUrl: String
)
