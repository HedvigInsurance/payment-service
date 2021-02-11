package com.hedvig.paymentservice.domain.accountRegistration.commands

import java.util.*
import org.axonframework.commandhandling.TargetAggregateIdentifier

data class ReceiveAccountRegistrationNotificationCommand(
    @TargetAggregateIdentifier  
    val accountRegistrationId: UUID,
    val memberId: String
)
