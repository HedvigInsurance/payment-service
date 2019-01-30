package com.hedvig.paymentservice.domain.trustlyOrder.events

import java.util.UUID
import lombok.Value

@Value
class AccountNotificationReceivedEvent {

    val hedvigOrderId: UUID? = null
    val memberId: String? = null

    val notificationId: String? = null
    val trustlyOrderId: String? = null

    val accountId: String? = null
    val address: String? = null
    val bank: String? = null
    val city: String? = null
    val clearingHouse: String? = null
    val descriptor: String? = null
    val directDebitMandate: Boolean? = null
    val lastDigits: String? = null
    val name: String? = null
    val personId: String? = null
    val zipCode: String? = null
}
