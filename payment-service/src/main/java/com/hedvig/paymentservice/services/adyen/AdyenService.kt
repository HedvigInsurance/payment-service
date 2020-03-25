package com.hedvig.paymentservice.services.adyen

import com.adyen.model.checkout.PaymentsDetailsRequest
import com.hedvig.paymentservice.graphQl.types.ActivePaymentMethodsResponse
import com.hedvig.paymentservice.graphQl.types.AvailablePaymentMethodsResponse
import com.hedvig.paymentservice.graphQl.types.TokenizationRequest
import com.hedvig.paymentservice.services.adyen.dtos.AdyenPaymentsResponse
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberRequest

interface AdyenService {
  fun getAvailablePaymentMethods(): AvailablePaymentMethodsResponse
  fun tokenizePaymentDetails(req: TokenizationRequest, memberId: String): AdyenPaymentsResponse
  fun chargeMemberWithToken(req: ChargeMemberRequest): Any
  fun getActivePaymentMethods(memberId: String): ActivePaymentMethodsResponse
  fun submitAdditionalPaymentDetails(req: PaymentsDetailsRequest): AdyenPaymentsResponse
}
