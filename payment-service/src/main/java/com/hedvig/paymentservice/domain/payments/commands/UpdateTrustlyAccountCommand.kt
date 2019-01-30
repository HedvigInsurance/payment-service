package com.hedvig.paymentservice.domain.payments.commands

import lombok.Value
import org.axonframework.commandhandling.TargetAggregateIdentifier

import java.util.UUID

@Value
class UpdateTrustlyAccountCommand {
    @TargetAggregateIdentifier
    val memberId: String? = null

    val hedvigOrderId: UUID? = null

    val accountId: String? = null
    val address: String? = null
    val bank: String? = null
    val city: String? = null
    val clearingHouse: String? = null
    val descriptor: String? = null
    val directDebitMandateActive: Boolean? = null
    val lastDigits: String? = null
    val name: String? = null
    val personId: String? = null
    val zipCode: String? = null
}
