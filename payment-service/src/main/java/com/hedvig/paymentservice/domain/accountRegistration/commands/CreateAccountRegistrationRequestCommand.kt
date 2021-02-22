package com.hedvig.paymentservice.domain.accountRegistration.commands

import java.util.*
import org.axonframework.commandhandling.TargetAggregateIdentifier

data class CreateAccountRegistrationRequestCommand(
    @TargetAggregateIdentifier  
    val accountRegistrationId: UUID,

    val hedvigOrderId: UUID,
    val memberId: String,
    val trustlyOrderId: String,
    val trustlyUrl: String
)
