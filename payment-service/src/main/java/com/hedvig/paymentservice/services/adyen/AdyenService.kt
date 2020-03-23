package com.hedvig.paymentservice.services.adyen

import com.adyen.model.checkout.PaymentMethodsResponse
import com.adyen.model.checkout.PaymentsRequest
import com.adyen.model.checkout.PaymentsResponse
import com.hedvig.paymentservice.graphQl.types.ActivePaymentMethodsResponse
import com.hedvig.paymentservice.graphQl.types.AvailablePaymentMethodsResponse
import com.hedvig.paymentservice.graphQl.types.TokenizationRequest
import com.hedvig.paymentservice.graphQl.types.TokenizationResponse
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberRequest

interface AdyenService {
  fun getAvailablePaymentMethods(): AvailablePaymentMethodsResponse
  fun tokenizePaymentDetails(req: TokenizationRequest, memberId: String): TokenizationResponse
  fun chargeMemberWithToken(req: ChargeMemberRequest): Any
  fun getActivePaymentMethods(memberId: String): ActivePaymentMethodsResponse
}
