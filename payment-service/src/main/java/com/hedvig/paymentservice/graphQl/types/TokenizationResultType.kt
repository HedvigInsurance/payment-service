package com.hedvig.paymentservice.graphQl.types

import com.hedvig.paymentservice.services.adyen.dtos.PaymentResponseResultCode

enum class TokenizationResultType {
    AUTHORISED,
    PENDING,
    FAILED;

    companion object {
        fun from(resultCode: PaymentResponseResultCode) = when (resultCode) {
            PaymentResponseResultCode.AUTHORISED -> AUTHORISED
            PaymentResponseResultCode.PENDING -> PENDING
            PaymentResponseResultCode.FAILED -> FAILED
        }
    }
}
