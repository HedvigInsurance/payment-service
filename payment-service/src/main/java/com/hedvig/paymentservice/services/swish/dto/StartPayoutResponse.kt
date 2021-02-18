package com.hedvig.paymentservice.services.swish.dto

sealed class StartPayoutResponse {
    object Success: StartPayoutResponse()
    data class Failed(
        val exceptionMessage: String?,
        val status: Int?
    ) : StartPayoutResponse()
}
