package com.hedvig.paymentservice.domain.trustlyOrder.commands

import java.time.Instant
import java.util.UUID
import javax.money.MonetaryAmount
import lombok.Value
import org.axonframework.commandhandling.TargetAggregateIdentifier

@Value
data class PendingNotificationReceivedCommand (
    @TargetAggregateIdentifier
    val hedvigOrderId: UUID,

    val notificationId: String,
    val trustlyOrderId: String,
    val amount: MonetaryAmount,
    val memberId: String,
    val timestamp: Instant
)
