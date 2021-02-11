package com.hedvig.paymentservice.domain.accountRegistration.commands

import java.util.*
import org.axonframework.commandhandling.TargetAggregateIdentifier

data class ReceiveAccountRegistrationResponseCommand(
    @TargetAggregateIdentifier  
    val accountRegistrationId: UUID
)
