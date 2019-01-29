package com.hedvig.paymentservice.domain.registerAccount.events

import java.util.*

data class RegisterAccountRequestCreatedEvent(
  val memberId: String,
  val hedvigOrderId: UUID
)
