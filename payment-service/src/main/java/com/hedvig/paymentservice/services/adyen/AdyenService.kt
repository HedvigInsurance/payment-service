package com.hedvig.paymentservice.services.adyen

import com.adyen.model.checkout.PaymentsDetailsRequest
import com.adyen.model.checkout.PaymentsResponse
import com.hedvig.paymentservice.graphQl.types.ActivePaymentMethodsResponse
import com.hedvig.paymentservice.graphQl.types.AvailablePaymentMethodsResponse
import com.hedvig.paymentservice.graphQl.types.SubmitAdyenRedirectionRequest
import com.hedvig.paymentservice.graphQl.types.SubmitAdyenRedirectionResponse
import com.hedvig.paymentservice.graphQl.types.TokenizationRequest
import com.hedvig.paymentservice.services.adyen.dtos.AdyenPaymentsResponse
import com.hedvig.paymentservice.services.adyen.dtos.ChargeMemberWithTokenRequest
import java.util.UUID

interface AdyenService {
  fun getAvailablePaymentMethods(): AvailablePaymentMethodsResponse
  fun tokenizePaymentDetails(req: TokenizationRequest, memberId: String, endUserIp: String?): AdyenPaymentsResponse
  fun chargeMemberWithToken(req: ChargeMemberWithTokenRequest): PaymentsResponse
  fun getActivePaymentMethods(memberId: String): ActivePaymentMethodsResponse?
  fun submitAdditionalPaymentDetails(req: PaymentsDetailsRequest, memberId: String): AdyenPaymentsResponse
  fun submitAdyenRedirection(req: SubmitAdyenRedirectionRequest, memberId: String): SubmitAdyenRedirectionResponse
  fun fetchAdyenPublicKey(): String
  fun handleSettlementError(adyenTransactionId: UUID)
}
