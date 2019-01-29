package com.hedvig.paymentservice.domain.registerAccount.commands

import java.util.*

data class ReceiveRegisterAccountResponseCommand(
  val hedvigOrderId: UUID,
  val trustlyUrl: String
)
