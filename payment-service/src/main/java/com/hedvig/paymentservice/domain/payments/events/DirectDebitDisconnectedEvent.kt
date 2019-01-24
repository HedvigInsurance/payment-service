package com.hedvig.paymentservice.domain.payments.events

import lombok.Value

//@Value
data class DirectDebitDisconnectedEvent (

    val memberId: String,
    val trustlyAccountId: String
)
