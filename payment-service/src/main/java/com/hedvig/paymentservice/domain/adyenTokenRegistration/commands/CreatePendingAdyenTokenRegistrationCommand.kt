package com.hedvig.paymentservice.domain.adyenTokenRegistration.commands

import com.hedvig.paymentservice.services.adyen.dtos.AdyenMerchantInfo
import com.hedvig.paymentservice.services.adyen.dtos.AdyenPaymentsResponse
import java.util.UUID
import org.axonframework.commandhandling.TargetAggregateIdentifier

data class CreatePendingAdyenTokenRegistrationCommand(
    @TargetAggregateIdentifier  
    val adyenTokenRegistrationId: UUID,
    val memberId: String,
    val adyenMerchantInfo: AdyenMerchantInfo,
    val adyenPaymentsResponse: AdyenPaymentsResponse,
    val paymentDataFromAction: String,
    val isPayoutSetup: Boolean = false,
    val shopperReference: String
)
