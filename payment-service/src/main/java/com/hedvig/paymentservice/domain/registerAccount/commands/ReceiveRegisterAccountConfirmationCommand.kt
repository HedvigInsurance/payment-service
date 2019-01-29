package com.hedvig.paymentservice.domain.registerAccount.commands

import java.util.*

data class ReceiveRegisterAccountConfirmationCommand(
  val hedvigOrderId: UUID,
  val memberId: String
)
