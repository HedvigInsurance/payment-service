package com.hedvig.paymentservice.domain.payments

import java.time.Instant
import java.util.*

data class DirectDebitAccountOrder(
    val hedvigOrderId: UUID,
    val account: TrustlyAccount,
    val createdAt: Instant
)
