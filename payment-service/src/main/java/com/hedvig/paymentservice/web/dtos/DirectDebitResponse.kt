package com.hedvig.paymentservice.web.dtos

data class DirectDebitResponse(
    val url: String,
    val orderId: String
)
