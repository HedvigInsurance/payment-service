package com.hedvig.paymentservice.services.swish

data class PayoutRequest(
    val payload: PayoutPayload,
    val signature: String,
    val callbackUrl: String
)
