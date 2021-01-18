package com.hedvig.paymentservice.services.adyen

import com.adyen.model.checkout.PaymentsDetailsRequest
import com.adyen.model.checkout.PaymentsResponse
import com.adyen.model.payout.ConfirmThirdPartyResponse
import com.adyen.model.payout.SubmitResponse
import com.hedvig.paymentservice.domain.adyenTokenRegistration.enums.AdyenTokenRegistrationStatus
import com.hedvig.paymentservice.graphQl.types.ActivePaymentMethodsResponse
import com.hedvig.paymentservice.graphQl.types.AvailablePaymentMethodsResponse
import com.hedvig.paymentservice.graphQl.types.PayoutMethodStatus
import com.hedvig.paymentservice.graphQl.types.SubmitAdyenRedirectionRequest
import com.hedvig.paymentservice.graphQl.types.SubmitAdyenRedirectionResponse
import com.hedvig.paymentservice.graphQl.types.TokenizationRequest
import com.hedvig.paymentservice.services.adyen.dtos.AdyenPaymentsResponse
import com.hedvig.paymentservice.services.adyen.dtos.ChargeMemberWithTokenRequest
import com.hedvig.paymentservice.web.dtos.adyen.NotificationRequestItem
import java.util.UUID
import javax.money.MonetaryAmount

interface AdyenService {
    fun getAvailablePayinMethods(memberId: String): AvailablePaymentMethodsResponse
    fun getAvailablePayoutMethods(memberId: String): AvailablePaymentMethodsResponse
    fun tokenizePaymentDetails(request: TokenizationRequest, memberId: String, endUserIp: String?): AdyenPaymentsResponse
    fun tokenizePayoutDetails(request: TokenizationRequest, memberId: String, endUserIp: String?): AdyenPaymentsResponse
    fun chargeMemberWithToken(request: ChargeMemberWithTokenRequest): PaymentsResponse
    fun getActivePayinMethods(memberId: String): ActivePaymentMethodsResponse?
    fun getLatestTokenRegistrationStatus(memberId: String): PayoutMethodStatus?
    fun submitAdditionalPaymentDetails(request: PaymentsDetailsRequest, memberId: String): AdyenPaymentsResponse
    fun submitAdyenRedirection(request: SubmitAdyenRedirectionRequest, memberId: String): SubmitAdyenRedirectionResponse
    fun fetchAdyenPublicKey(): String
    fun handleSettlementErrorNotification(adyenTransactionId: UUID)
    fun handleAuthorisationNotification(adyenNotification: NotificationRequestItem)
    fun handleRecurringContractNotification(adyenNotification: NotificationRequestItem)
    fun startPayoutTransaction(
        memberId: String,
        payoutReference: String,
        amount: MonetaryAmount,
        shopperReference: String,
        shopperEmail: String
    ): SubmitResponse

    fun confirmPayout(payoutReference: String, memberId: String): ConfirmThirdPartyResponse
    fun handlePayoutThirdPartyNotification(adyenNotification: NotificationRequestItem)
    fun handlePayoutDeclinedNotification(adyenNotification: NotificationRequestItem)
    fun handlePayoutExpireNotification(adyenNotification: NotificationRequestItem)
    fun handlePayoutPaidOutReservedNotification(adyenNotification: NotificationRequestItem)
    fun handleAutoRescueNotification(adyenNotification: NotificationRequestItem): Any
}
