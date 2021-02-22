package com.hedvig.paymentservice.domain.trustlyOrder.commands

import java.util.UUID
import org.axonframework.commandhandling.TargetAggregateIdentifier

data class CancelNotificationReceivedCommand(
    @TargetAggregateIdentifier
    val hedvigOrderId: UUID,

    val notificationId: String,
    val trustlyOrderId: String,
    val memberId: String
)
