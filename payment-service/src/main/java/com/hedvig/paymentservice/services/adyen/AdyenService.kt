package com.hedvig.paymentservice.services.adyen

import com.adyen.model.checkout.PaymentMethodsResponse
import com.adyen.model.checkout.PaymentsRequest
import com.adyen.model.checkout.PaymentsResponse
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberRequest

interface AdyenService {
  fun getAvailablePaymentMethods(): PaymentMethodsResponse
  fun tokenizePaymentDetails(req: PaymentsRequest, memberId: String): PaymentsResponse
  fun chargeMemberWithToken(req: ChargeMemberRequest): Any
  fun getActivePaymentMethods(memberId: String): PaymentMethodsResponse
}
