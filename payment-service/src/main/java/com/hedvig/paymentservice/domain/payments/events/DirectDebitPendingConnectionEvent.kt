package com.hedvig.paymentservice.domain.payments.events

data class DirectDebitPendingConnectionEvent(
    val memberId: String,
    val hedvigOrderId: String,
    val trustlyAccountId: String
)
