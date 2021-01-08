package com.hedvig.paymentservice.domain.trustlyOrder.events

import java.time.Instant
import java.util.*
import javax.money.MonetaryAmount

data class PendingNotificationReceivedEvent (
    val hedvigOrderId: UUID,
    val notificationId: String,
    val trustlyOrderId: String,
    val amount: MonetaryAmount,
    val memberId: String,
    val timestamp: Instant,
)
