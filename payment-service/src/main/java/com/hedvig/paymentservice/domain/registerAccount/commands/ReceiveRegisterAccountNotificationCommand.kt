package com.hedvig.paymentservice.domain.registerAccount.commands

import java.util.*

data class ReceiveRegisterAccountNotificationCommand(
  val hedvigOrderId: UUID,
  val memberId: String
)
