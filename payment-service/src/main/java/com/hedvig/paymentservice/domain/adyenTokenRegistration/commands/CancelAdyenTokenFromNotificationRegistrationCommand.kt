package com.hedvig.paymentservice.domain.adyenTokenRegistration.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.UUID

data class CancelAdyenTokenFromNotificationRegistrationCommand(
    @TargetAggregateIdentifier
    val adyenTokenRegistrationId: UUID,
    val memberId: String
)
