package com.hedvig.paymentservice.domain.payments.commands

import java.util.*
import lombok.Value
import org.axonframework.commandhandling.TargetAggregateIdentifier

@Value
class UpdateTrustlyAccountCommand(
    @TargetAggregateIdentifier  
    val memberId: String,

    val hedvigOrderId: UUID,

    val accountId: String,
    val address: String?,
    val bank: String?,
    val city: String?,
    val clearingHouse: String?,
    val descriptor: String?,
    val directDebitMandateActive: Boolean?,
    val lastDigits: String?,
    val name: String?,
    val personId: String?,
    val zipCode: String?
)
