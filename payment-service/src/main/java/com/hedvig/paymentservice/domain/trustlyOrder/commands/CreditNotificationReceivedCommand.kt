package com.hedvig.paymentservice.domain.trustlyOrder.commands

import java.time.Instant
import java.util.UUID
import javax.money.MonetaryAmount
import org.axonframework.commandhandling.TargetAggregateIdentifier

class CreditNotificationReceivedCommand(
    @TargetAggregateIdentifier
    val hedvigOrderId: UUID,

    val notificationId: String,
    val trustlyOrderId: String,
    val memberId: String,
    val amount: MonetaryAmount,
    val timestamp: Instant
)
