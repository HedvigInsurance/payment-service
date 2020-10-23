package com.hedvig.paymentservice.services.adyen

import com.adyen.model.checkout.PaymentsDetailsRequest
import com.adyen.model.checkout.PaymentsResponse
import com.adyen.model.payout.ConfirmThirdPartyResponse
import com.adyen.model.payout.SubmitResponse
import com.hedvig.paymentservice.graphQl.types.ActivePaymentMethodsResponse
import com.hedvig.paymentservice.graphQl.types.AvailablePaymentMethodsResponse
import com.hedvig.paymentservice.graphQl.types.SubmitAdyenRedirectionRequest
import com.hedvig.paymentservice.graphQl.types.SubmitAdyenRedirectionResponse
import com.hedvig.paymentservice.graphQl.types.TokenizationRequest
import com.hedvig.paymentservice.services.adyen.dtos.AdyenPaymentsResponse
import com.hedvig.paymentservice.services.adyen.dtos.ChargeMemberWithTokenRequest
import com.hedvig.paymentservice.web.dtos.adyen.NotificationRequestItem
import java.util.UUID
import javax.money.MonetaryAmount

interface AdyenService {
  fun getAvailablePaymentMethods(): AvailablePaymentMethodsResponse
  fun tokenizePaymentDetails(req: TokenizationRequest, memberId: String, endUserIp: String?): AdyenPaymentsResponse
  fun tokenizePayoutDetails(req: TokenizationRequest, memberId: String, endUserIp: String?): AdyenPaymentsResponse
  fun chargeMemberWithToken(req: ChargeMemberWithTokenRequest): PaymentsResponse
  fun getActivePaymentMethods(memberId: String): ActivePaymentMethodsResponse?
  fun submitAdditionalPaymentDetails(req: PaymentsDetailsRequest, memberId: String): AdyenPaymentsResponse
  fun submitAdyenRedirection(req: SubmitAdyenRedirectionRequest, memberId: String): SubmitAdyenRedirectionResponse
  fun fetchAdyenPublicKey(): String
  fun handleSettlementErrorNotification(adyenTransactionId: UUID)
  fun handleAuthorisationNotification(adyenNotification: NotificationRequestItem)
  fun handleRecurringContractNotification(adyenNotification: NotificationRequestItem)
  fun startPayoutTransaction(
    payoutReference: String,
    amount: MonetaryAmount,
    shopperReference: String,
    shopperEmail: String
  ): SubmitResponse

  fun confirmPayout(payoutReference: String): ConfirmThirdPartyResponse
  fun handlePayoutThirdPartyNotification(adyenNotification: NotificationRequestItem)
  fun handlePayoutDeclinedNotification(adyenNotification: NotificationRequestItem)
  fun handlePayoutExpireNotification(adyenNotification: NotificationRequestItem)
  fun handlePayoutPaidOutReservedNotification(adyenNotification: NotificationRequestItem)
}
