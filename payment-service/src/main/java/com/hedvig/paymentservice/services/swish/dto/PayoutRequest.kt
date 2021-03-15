package com.hedvig.paymentservice.services.swish.dto

data class PayoutRequest(
    val payload: PayoutPayload,
    val signature: String,
    val callbackUrl: String
)
