package com.hedvig.paymentservice.domain.payments.commands

import com.hedvig.paymentservice.domain.adyenTokenRegistration.enums.AdyenTokenRegistrationStatus
import org.axonframework.commandhandling.TargetAggregateIdentifier

data class UpdateAdyenPayoutAccountCommand(
    @TargetAggregateIdentifier  
    val memberId: String,
    val shopperReference: String,
    val adyenTokenStatus: AdyenTokenRegistrationStatus
)
