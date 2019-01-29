package com.hedvig.paymentservice.domain.registerAccount.commands

import java.util.UUID

data class CreateRegisterAccountRequestCommand(
  val memberId: String,
  val hedvigOrderId: UUID
)
