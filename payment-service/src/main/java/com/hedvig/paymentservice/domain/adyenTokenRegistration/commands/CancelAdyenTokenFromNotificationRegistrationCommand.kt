package com.hedvig.paymentservice.domain.adyenTokenRegistration.commands

import java.util.UUID
import org.axonframework.commandhandling.TargetAggregateIdentifier

data class CancelAdyenTokenFromNotificationRegistrationCommand(
    @TargetAggregateIdentifier
    val adyenTokenRegistrationId: UUID,
    val memberId: String
)
