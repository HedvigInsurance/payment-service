package com.hedvig.paymentservice.services.adyen.dtos

import com.adyen.model.checkout.PaymentsResponse
import com.adyen.model.checkout.PaymentsResponse.ResultCodeEnum.AUTHENTICATIONFINISHED
import com.adyen.model.checkout.PaymentsResponse.ResultCodeEnum.AUTHENTICATIONNOTREQUIRED
import com.adyen.model.checkout.PaymentsResponse.ResultCodeEnum.AUTHORISED
import com.adyen.model.checkout.PaymentsResponse.ResultCodeEnum.CANCELLED
import com.adyen.model.checkout.PaymentsResponse.ResultCodeEnum.CHALLENGESHOPPER
import com.adyen.model.checkout.PaymentsResponse.ResultCodeEnum.ERROR
import com.adyen.model.checkout.PaymentsResponse.ResultCodeEnum.IDENTIFYSHOPPER
import com.adyen.model.checkout.PaymentsResponse.ResultCodeEnum.PARTIALLYAUTHORISED
import com.adyen.model.checkout.PaymentsResponse.ResultCodeEnum.PENDING
import com.adyen.model.checkout.PaymentsResponse.ResultCodeEnum.PRESENTTOSHOPPER
import com.adyen.model.checkout.PaymentsResponse.ResultCodeEnum.RECEIVED
import com.adyen.model.checkout.PaymentsResponse.ResultCodeEnum.REDIRECTSHOPPER
import com.adyen.model.checkout.PaymentsResponse.ResultCodeEnum.REFUSED
import com.adyen.model.checkout.PaymentsResponse.ResultCodeEnum.UNKNOWN

data class AdyenPaymentsResponse(
  val paymentsResponse: PaymentsResponse
) {
  fun getRecurringDetailReference(): String? = paymentsResponse.getAdditionalDataByKey(RECURRING_DETAIL_REFERENCE)

  fun getResultCode(): PaymentResponseResultCode {
    return when (this.paymentsResponse.resultCode) {
      AUTHORISED -> PaymentResponseResultCode.AUTHORISED

      AUTHENTICATIONFINISHED,
      AUTHENTICATIONNOTREQUIRED,
      CHALLENGESHOPPER,
      IDENTIFYSHOPPER,
      PENDING,
      RECEIVED,
      REDIRECTSHOPPER,
      PARTIALLYAUTHORISED,
      PRESENTTOSHOPPER,
      UNKNOWN -> {
        PaymentResponseResultCode.PENDING
      }

      ERROR,
      CANCELLED,
      REFUSED -> {
        PaymentResponseResultCode.FAILED
      }
      else -> throw RuntimeException("Cannot translate ResultCodeEnum to PaymentResponseResultCode  [PaymentsResponse: ${this.paymentsResponse} ] ")
    }
  }

  companion object {
    const val RECURRING_DETAIL_REFERENCE: String = "recurring.recurringDetailReference"
  }
}
