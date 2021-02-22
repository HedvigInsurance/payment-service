package com.hedvig.paymentservice.graphQl.types

data class SubmitAdyenRedirectionRequest(
    val md: String,
    val pares: String
)
