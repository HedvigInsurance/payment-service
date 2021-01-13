package com.hedvig.paymentservice.graphQl.types

import com.hedvig.paymentservice.web.dtos.DirectDebitResponse as DirectDebitResponseDTO

data class DirectDebitResponse(
    val url: String,
    val orderId: String
) {
    companion object {
        fun fromDirectDebitResponse(directDebitResponse: DirectDebitResponseDTO) = DirectDebitResponse(
            url = directDebitResponse.url,
            orderId = directDebitResponse.orderId
        )
    }
}
