package com.hedvig.paymentservice.domain.adyenTokenRegistration.commands

import com.hedvig.paymentservice.services.adyen.dtos.AdyenPaymentsResponse
import java.util.UUID
import org.axonframework.commandhandling.TargetAggregateIdentifier

data class UpdatePendingAdyenTokenRegistrationCommand(
    @TargetAggregateIdentifier  
    val adyenTokenRegistrationId: UUID,
    val memberId: String,
    val adyenPaymentsResponse: AdyenPaymentsResponse
)
