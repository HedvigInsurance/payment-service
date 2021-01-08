package com.hedvig.paymentservice.domain.adyenTransaction.events

import java.util.UUID
import javax.money.MonetaryAmount

class AdyenTransactionAuthorisationResponseReceivedEvent(
    val transactionId: UUID,
    val memberId: String,
    val amount: MonetaryAmount,
    val rescueReference: String?
)
