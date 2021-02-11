package com.hedvig.paymentservice.domain.accountRegistration.events

import java.util.*

data class AccountRegistrationCancellationReceivedEvent(
    val accountRegistrationId: UUID,
    val hedvigOrderId: UUID,
    val memberId: String
)
