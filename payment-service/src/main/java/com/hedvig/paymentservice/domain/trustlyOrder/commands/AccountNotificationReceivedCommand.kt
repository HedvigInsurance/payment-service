package com.hedvig.paymentservice.domain.trustlyOrder.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.UUID

data class AccountNotificationReceivedCommand(
    @TargetAggregateIdentifier
    val hedvigOrderId: UUID,

    val notificationId: String,
    val trustlyOrderId: String,
    val accountId: String,
    val address: String?,
    val bank: String?,
    val city: String?,
    val clearingHouse: String?,
    val descriptor: String?,
    val directDebitMandateActivated: Boolean?,
    val lastDigits: String?,
    val name: String?,
    val personId: String?,
    val zipCode: String?
)
