package com.hedvig.paymentservice.domain.adyenTokenRegistration.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.*

data class SetAdyenTokenRegistrationToPendingFromNotificationCommand(
    @TargetAggregateIdentifier
    val adyenTokenRegistrationId : UUID,
    val memberId: String
)
