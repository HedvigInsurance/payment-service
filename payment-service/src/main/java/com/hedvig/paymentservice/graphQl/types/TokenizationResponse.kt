package com.hedvig.paymentservice.graphQl.types

import com.adyen.model.checkout.PaymentsResponse

data class TokenizationResponse(
  val paymentsResponse: PaymentsResponse
) {

  fun getRecurringDetailReference(): String? = paymentsResponse.getAdditionalDataByKey(RECURRING_DETAIL_REFERENCE)

  fun getTokenStatus(): PaymentsResponse.ResultCodeEnum = paymentsResponse.resultCode

  companion object {
    const val RECURRING_DETAIL_REFERENCE: String = "recurring.recurringDetailReference"
  }
}
