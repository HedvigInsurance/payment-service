package com.hedvig.paymentservice.domain.registerAccount.events

import java.util.*

data class RegisterAccountNotificationReceivedEvent(
  val hedvigOrderId: UUID,
  val memberId: String
)
