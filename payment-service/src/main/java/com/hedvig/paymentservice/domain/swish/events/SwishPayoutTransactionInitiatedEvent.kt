package com.hedvig.paymentservice.domain.swish.events

import java.util.UUID
import javax.money.MonetaryAmount

class SwishPayoutTransactionInitiatedEvent(
    val transactionId: UUID,
    val memberId: String,
    val phoneNumber: String,
    val ssn: String,
    val message: String,
    val amount: MonetaryAmount
)
