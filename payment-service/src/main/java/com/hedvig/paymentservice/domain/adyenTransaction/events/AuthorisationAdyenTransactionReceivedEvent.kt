package com.hedvig.paymentservice.domain.adyenTransaction.events

import java.util.UUID

class AuthorisationAdyenTransactionReceivedEvent(
  val transactionId: UUID,
  val memberId: String
)
