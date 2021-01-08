package com.hedvig.paymentservice.domain.trustlyOrder.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.time.LocalDate
import java.util.*
import javax.money.MonetaryAmount

data class CreatePayoutOrderCommand(
    @TargetAggregateIdentifier
    val hedvigOrderId: UUID,
    val transactionId: UUID,
    val memberId: String,
    val amount: MonetaryAmount,
    val trustlyAccountId: String,
    val address: String?,
    val countryCode: String?,
    val dateOfBirth: LocalDate?,
    val firstName: String?,
    val lastName: String?,
)
