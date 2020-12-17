package com.hedvig.paymentservice.domain.payments

import java.time.Instant
import java.util.*

data class TrustlyOrder(
    val hedvigOrderID: UUID,
    val account: TrustlyAccount,
    val createdAt: Instant
)
