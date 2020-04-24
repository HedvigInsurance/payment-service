package com.hedvig.paymentservice.domain.adyenTransaction.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.UUID

class ReceiveCaptureFailureAdyenTransactionCommand(
  @TargetAggregateIdentifier
  val transactionId: UUID,
  val memberId: String
)
