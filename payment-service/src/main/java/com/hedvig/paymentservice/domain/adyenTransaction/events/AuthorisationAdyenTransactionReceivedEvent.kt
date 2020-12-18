package com.hedvig.paymentservice.domain.adyenTransaction.events

import org.axonframework.serialization.Revision
import java.util.UUID

@Revision("1.0")
class AuthorisationAdyenTransactionReceivedEvent(
  val transactionId: UUID,
  val memberId: String,
  val rescueReference: String?
)
