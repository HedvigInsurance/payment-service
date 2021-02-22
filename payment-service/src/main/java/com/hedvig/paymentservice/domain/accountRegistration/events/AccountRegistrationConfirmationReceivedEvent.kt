package com.hedvig.paymentservice.domain.accountRegistration.events

import java.util.*

data class AccountRegistrationConfirmationReceivedEvent(
    val accountRegistrationId: UUID,
    val memberId: String
)
