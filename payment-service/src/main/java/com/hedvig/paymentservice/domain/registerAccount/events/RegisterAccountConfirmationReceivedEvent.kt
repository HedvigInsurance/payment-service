package com.hedvig.paymentservice.domain.registerAccount.events

import java.util.*

data class RegisterAccountConfirmationReceivedEvent(
  val hedvigOrderId: UUID,
  val memberId: String
)
