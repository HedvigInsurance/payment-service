package com.hedvig.paymentservice.domain.trustlyOrder.events

import com.hedvig.paymentservice.domain.trustlyOrder.OrderType
import lombok.Value
import java.time.Instant
import java.util.*
import javax.money.MonetaryAmount

data class CreditNotificationReceivedEvent (
    val hedvigOrderId: UUID,
    val transactionId: UUID,
    val notificationId: String,
    val trustlyOrderId: String,
    val memberId: String,
    val amount: MonetaryAmount,
    val timestamp: Instant,
    val orderType: OrderType,
)
