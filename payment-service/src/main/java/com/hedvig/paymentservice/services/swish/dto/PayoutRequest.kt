package com.hedvig.paymentservice.services.swish.dto

import com.hedvig.paymentservice.services.swish.dto.PayoutPayload

data class PayoutRequest(
    val payload: PayoutPayload,
    val signature: String,
    val callbackUrl: String
)
