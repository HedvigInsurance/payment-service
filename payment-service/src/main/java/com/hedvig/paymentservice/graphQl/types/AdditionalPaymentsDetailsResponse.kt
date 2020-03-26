package com.hedvig.paymentservice.graphQl.types

import com.adyen.model.checkout.CheckoutPaymentsAction

sealed class AdditionalPaymentsDetailsResponse {
  data class AdditionalPaymentsDetailsResponseFinished(
    val resultCode: String
  ) : AdditionalPaymentsDetailsResponse()

  data class AdditionalPaymentsDetailsResponseAction(
    val action: CheckoutPaymentsAction
  ) : AdditionalPaymentsDetailsResponse()
}
