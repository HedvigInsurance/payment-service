package com.hedvig.paymentservice.domain.swish.events

import java.util.UUID

class SwishPayoutTransactionFailedEvent(
    val transactionId: UUID,
    val memberId: String,
    val errorCode :String?,
    val errorMessage :String?,
    val additionalInformation :String?
)
