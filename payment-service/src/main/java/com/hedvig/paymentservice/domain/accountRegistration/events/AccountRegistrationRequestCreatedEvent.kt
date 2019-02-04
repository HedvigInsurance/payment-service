package com.hedvig.paymentservice.domain.accountRegistration.events

import java.util.*

data class AccountRegistrationRequestCreatedEvent(
  val accountRegistrationId: UUID,
  val hedvigOrderId: UUID,
  val memberId: String,
  val trustlyOrderId: String,
  val trustlyUrl: String
)
