package com.hedvig.paymentservice.domain.adyenTransaction.events

import java.util.UUID

class AdyenTransactionPendingResponseReceivedEvent(
  val transactionId: UUID,
  val reason: String
)
