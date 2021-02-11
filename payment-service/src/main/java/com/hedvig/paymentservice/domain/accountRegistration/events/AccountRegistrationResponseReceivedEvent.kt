package com.hedvig.paymentservice.domain.accountRegistration.events

import java.util.*

data class AccountRegistrationResponseReceivedEvent(
    val accountRegistrationId: UUID
)
