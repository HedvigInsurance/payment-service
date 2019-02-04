package com.hedvig.paymentservice.domain.accountRegistration.events

import java.util.*

data class AccountRegistrationNotificationReceivedEvent(
  val accountRegistrationId: UUID,
  val memberId: String
)
