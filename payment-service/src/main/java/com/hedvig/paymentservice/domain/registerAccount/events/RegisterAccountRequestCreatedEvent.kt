package com.hedvig.paymentservice.domain.registerAccount.events

import java.util.*

data class RegisterAccountRequestCreatedEvent(
  val hedvigOrderId: UUID,
  val memberId: String
)
