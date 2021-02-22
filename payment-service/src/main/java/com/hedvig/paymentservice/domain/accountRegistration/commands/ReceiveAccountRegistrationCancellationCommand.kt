package com.hedvig.paymentservice.domain.accountRegistration.commands

import java.util.*
import org.axonframework.commandhandling.TargetAggregateIdentifier

data class ReceiveAccountRegistrationCancellationCommand(
    @TargetAggregateIdentifier  
    val accountRegistrationId: UUID,

    val hedvigOrderId: UUID,
    val memberId: String
)
