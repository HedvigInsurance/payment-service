package com.hedvig.paymentservice.domain.trustlyOrder.events

import java.util.UUID

data class NotificationReceivedEvent(
    val hedvigOrderId: UUID,
    val notificationId: String,
    val trustlyOrderId: String
)
