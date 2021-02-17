package com.hedvig.paymentservice.services.swish

sealed class StartPayoutResponse {
    object Success: StartPayoutResponse()
    data class Failed(
        val exceptionMessage: String?,
        val status: Int?
    ) : StartPayoutResponse()
}
