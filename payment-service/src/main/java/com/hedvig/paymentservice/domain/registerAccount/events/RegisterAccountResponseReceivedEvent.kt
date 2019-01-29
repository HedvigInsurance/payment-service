package com.hedvig.paymentservice.domain.registerAccount.events

import java.util.*

data class RegisterAccountResponseReceivedEvent(
  val hedvigOrderId: UUID,
  val trustlyUrl: String
)
