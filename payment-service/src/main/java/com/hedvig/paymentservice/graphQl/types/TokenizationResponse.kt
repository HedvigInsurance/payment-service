package com.hedvig.paymentservice.graphQl.types

import com.adyen.model.checkout.CheckoutPaymentsAction

sealed class TokenizationResponse() {

    data class TokenizationResponseFinished(
        val resultCode: String,
        val activePayoutMethods: ActivePayoutMethodsResponse?
    ) : TokenizationResponse()

    data class TokenizationResponseAction(
        val action: CheckoutPaymentsAction
    ) : TokenizationResponse()
}
