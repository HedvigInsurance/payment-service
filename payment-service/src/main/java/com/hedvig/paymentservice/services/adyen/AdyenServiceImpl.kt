package com.hedvig.paymentservice.services.adyen

import com.adyen.constants.ApiConstants
import com.adyen.model.Amount
import com.adyen.model.BrowserInfo as AdyenBrowserInfo
import com.adyen.model.checkout.DefaultPaymentMethodDetails
import com.adyen.model.checkout.PaymentMethod
import com.adyen.model.checkout.PaymentMethodsRequest
import com.adyen.model.checkout.PaymentMethodsResponse
import com.adyen.model.checkout.PaymentsDetailsRequest
import com.adyen.model.checkout.PaymentsRequest
import com.adyen.model.checkout.PaymentsRequest.RecurringProcessingModelEnum
import com.adyen.model.checkout.PaymentsResponse
import com.adyen.model.checkout.StoredPaymentMethod
import com.adyen.model.payout.ConfirmThirdPartyRequest
import com.adyen.model.payout.ConfirmThirdPartyResponse
import com.adyen.model.payout.SubmitRequest
import com.adyen.model.payout.SubmitResponse
import com.adyen.model.recurring.Recurring
import com.adyen.service.Checkout
import com.adyen.service.Payout
import com.hedvig.paymentservice.common.UUIDGenerator
import com.hedvig.paymentservice.domain.adyenTokenRegistration.commands.AuthoriseAdyenTokenRegistrationFromNotificationCommand
import com.hedvig.paymentservice.domain.adyenTokenRegistration.commands.AuthorisedAdyenTokenRegistrationCommand
import com.hedvig.paymentservice.domain.adyenTokenRegistration.commands.CancelAdyenTokenFromNotificationRegistrationCommand
import com.hedvig.paymentservice.domain.adyenTokenRegistration.commands.CancelAdyenTokenRegistrationCommand
import com.hedvig.paymentservice.domain.adyenTokenRegistration.commands.CreateAuthorisedAdyenTokenRegistrationCommand
import com.hedvig.paymentservice.domain.adyenTokenRegistration.commands.CreatePendingAdyenTokenRegistrationCommand
import com.hedvig.paymentservice.domain.adyenTokenRegistration.commands.UpdatePendingAdyenTokenRegistrationCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceiveAdyenTransactionUnsuccessfulRetryResponseCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceiveAuthorisationAdyenTransactionCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceiveCancellationResponseAdyenTransactionCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceiveCaptureFailureAdyenTransactionCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceivedAdyenTransactionAutoRescueProcessEndedFromNotificationCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceivedDeclinedAdyenPayoutTransactionFromNotificationCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceivedExpiredAdyenPayoutTransactionFromNotificationCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceivedFailedAdyenPayoutTransactionFromNotificationCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceivedReservedAdyenPayoutTransactionFromNotificationCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceivedSuccessfulAdyenPayoutTransactionFromNotificationCommand
import com.hedvig.paymentservice.domain.payments.commands.CreateMemberCommand
import com.hedvig.paymentservice.graphQl.types.ActivePaymentMethodsResponse
import com.hedvig.paymentservice.graphQl.types.AvailablePaymentMethodsResponse
import com.hedvig.paymentservice.graphQl.types.BrowserInfo
import com.hedvig.paymentservice.graphQl.types.PayoutMethodStatus
import com.hedvig.paymentservice.graphQl.types.SubmitAdyenRedirectionRequest
import com.hedvig.paymentservice.graphQl.types.SubmitAdyenRedirectionResponse
import com.hedvig.paymentservice.graphQl.types.TokenizationChannel
import com.hedvig.paymentservice.graphQl.types.TokenizationRequest
import com.hedvig.paymentservice.query.adyenAccount.MemberAdyenAccountRepository
import com.hedvig.paymentservice.query.adyenTokenRegistration.entities.AdyenTokenRegistration
import com.hedvig.paymentservice.query.adyenTokenRegistration.entities.AdyenTokenRegistrationRepository
import com.hedvig.paymentservice.query.adyenTransaction.entities.AdyenPayoutTransaction
import com.hedvig.paymentservice.query.adyenTransaction.entities.AdyenPayoutTransactionRepository
import com.hedvig.paymentservice.query.adyenTransaction.entities.AdyenTransaction
import com.hedvig.paymentservice.query.adyenTransaction.entities.AdyenTransactionRepository
import com.hedvig.paymentservice.query.member.entities.MemberRepository
import com.hedvig.paymentservice.serviceIntergration.memberService.MemberService
import com.hedvig.paymentservice.services.adyen.dtos.AdyenPaymentsResponse
import com.hedvig.paymentservice.services.adyen.dtos.ChargeMemberWithTokenRequest
import com.hedvig.paymentservice.services.adyen.dtos.HedvigPaymentMethodDetails
import com.hedvig.paymentservice.services.adyen.dtos.PaymentResponseResultCode
import com.hedvig.paymentservice.services.adyen.dtos.StoredPaymentMethodsDetails
import com.hedvig.paymentservice.services.adyen.extentions.NoMerchantAccountForMarket
import com.hedvig.paymentservice.services.adyen.util.AdyenMerchantPicker
import com.hedvig.paymentservice.web.dtos.adyen.NotificationRequestItem
import java.util.Optional
import java.util.UUID
import javax.money.MonetaryAmount
import kotlin.collections.set
import org.axonframework.commandhandling.gateway.CommandGateway
import org.javamoney.moneta.Money
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AdyenServiceImpl(
    val adyenCheckout: Checkout,
    @Qualifier("AdyenPayout")
    val adyenPayout: Payout,
    @Qualifier("AdyenPayoutConfirmation")
    val adyenPayoutConfirmation: Payout,
    val memberRepository: MemberRepository,
    val uuidGenerator: UUIDGenerator,
    val memberService: MemberService,
    val commandGateway: CommandGateway,
    val tokenRegistrationRepository: AdyenTokenRegistrationRepository,
    val transactionRepository: AdyenTransactionRepository,
    val adyenPayoutTransactionRepository: AdyenPayoutTransactionRepository,
    val adyenMerchantPicker: AdyenMerchantPicker,
    val memberAdyenAccountRepository: MemberAdyenAccountRepository,
    @param:Value("\${hedvig.adyen.allow3DS2}")
    val allow3DS2: Boolean,
    @param:Value("\${hedvig.adyen.allowTrustlyPayouts}")
    val allowTrustlyPayouts: Boolean,
    @param:Value("\${hedvig.adyen.public.key}")
    val adyenPublicKey: String,
    @param:Value("\${hedvig.adyen.charge.autorescue.scenario}")
    val autoRescueScenario: String?
) : AdyenService {
    override fun getAvailablePayinMethods(memberId: String): AvailablePaymentMethodsResponse {
        val response: PaymentMethodsResponse = getAvailablePaymentMethods(memberId)
        response.paymentMethods = excludeTrustlyFromAvailablePaymentMethods(response.paymentMethods)
        return AvailablePaymentMethodsResponse(paymentMethodsResponse = response)
    }

    override fun getAvailablePayoutMethods(memberId: String): AvailablePaymentMethodsResponse {
        val response: PaymentMethodsResponse = getAvailablePaymentMethods(memberId)
        response.paymentMethods = includeOnlyTrustlyFromAvailablePayoutMethods(response.paymentMethods)
        if (!allowTrustlyPayouts) {
            response.paymentMethods = excludeTrustlyFromAvailablePayoutMethods(response.paymentMethods)
        }
        return AvailablePaymentMethodsResponse(paymentMethodsResponse = response)
    }

    override fun tokenizePaymentDetails(
        request: TokenizationRequest,
        memberId: String,
        endUserIp: String?
    ): AdyenPaymentsResponse {
        val adyenMerchantInfo = adyenMerchantPicker.getAdyenMerchantInfo(memberId)
        val (adyenTokenId, paymentsRequest) = createTokenizePaymentsRequest(
            request = request,
            memberId = memberId,
            endUserIp = endUserIp,
            shopperReference = memberId,
            isSubscription = true
        )

        val response = performCheckout(paymentsRequest, memberId, request)

        when (response.getResultCode()) {
            PaymentResponseResultCode.AUTHORISED -> {
                commandGateway.sendAndWait<Void>(
                    CreateAuthorisedAdyenTokenRegistrationCommand(
                        memberId = memberId,
                        adyenTokenRegistrationId = adyenTokenId,
                        adyenPaymentsResponse = response,
                        adyenMerchantInfo = adyenMerchantInfo,
                        shopperReference = memberId
                    )
                )
            }
            PaymentResponseResultCode.PENDING -> {
                commandGateway.sendAndWait<Void>(
                    CreatePendingAdyenTokenRegistrationCommand(
                        memberId = memberId,
                        adyenTokenRegistrationId = adyenTokenId,
                        adyenPaymentsResponse = response,
                        adyenMerchantInfo = adyenMerchantInfo,
                        paymentDataFromAction = response.paymentsResponse.action.paymentData,
                        shopperReference = memberId
                    )
                )
            }
            PaymentResponseResultCode.FAILED -> {
                logger.error("Tokenizing payment method failed [MemberId: $memberId] [Request: $request] [Response: $response]")
            }
        }
        return response
    }

    override fun tokenizePayoutDetails(
        request: TokenizationRequest,
        memberId: String,
        endUserIp: String?
    ): AdyenPaymentsResponse {
        val shopperReference = "payout_${memberId}_${UUID.randomUUID()}"

        val adyenMerchantInfo = adyenMerchantPicker.getAdyenMerchantInfo(memberId)
        val (adyenTokenId, paymentsRequest) = createTokenizePaymentsRequest(
            request = request,
            memberId = memberId,
            endUserIp = endUserIp,
            shopperReference = shopperReference,
            isSubscription = false
        )

        paymentsRequest.enablePayOut(true)

        val response = performCheckout(paymentsRequest, memberId, request)

        when (response.getResultCode()) {
            PaymentResponseResultCode.AUTHORISED -> {
                commandGateway.sendAndWait<Void>(
                    CreateAuthorisedAdyenTokenRegistrationCommand(
                        memberId = memberId,
                        adyenTokenRegistrationId = adyenTokenId,
                        adyenPaymentsResponse = response,
                        adyenMerchantInfo = adyenMerchantInfo,
                        isPayoutSetup = true,
                        shopperReference = shopperReference
                    )
                )
            }
            PaymentResponseResultCode.PENDING -> {
                commandGateway.sendAndWait<Void>(
                    CreatePendingAdyenTokenRegistrationCommand(
                        memberId = memberId,
                        adyenTokenRegistrationId = adyenTokenId,
                        adyenPaymentsResponse = response,
                        paymentDataFromAction = response.paymentsResponse.action.paymentData,
                        adyenMerchantInfo = adyenMerchantInfo,
                        isPayoutSetup = true,
                        shopperReference = shopperReference
                    )
                )
            }
            PaymentResponseResultCode.FAILED -> {
                logger.error("Tokenizing payment method failed [MemberId: $memberId] [Request: $request] [Response: $response]")
            }
        }

        return response
    }

    private fun performCheckout(paymentsRequest: PaymentsRequest, memberId: String, req: TokenizationRequest) = try {
        val res = adyenCheckout.payments(paymentsRequest)
        AdyenPaymentsResponse(paymentsResponse = res)
    } catch (exception: Exception) {
        logger.error("Tokenization with Adyen exploded 💥 [MemberId: $memberId] [Request: $req]", exception)
        throw exception
    }

    private fun createTokenizePaymentsRequest(
        request: TokenizationRequest,
        memberId: String,
        endUserIp: String?,
        shopperReference: String,
        isSubscription: Boolean
    ): Pair<UUID, PaymentsRequest> {
        val optionalMember = memberService.getMember(memberId)
        require(optionalMember.isPresent) { "Member not found" }

        createMember(memberId)
        val adyenMerchantInfo = adyenMerchantPicker.getAdyenMerchantInfo(memberId)
        val adyenTokenId = uuidGenerator.generateRandom()

        val paymentsRequest = PaymentsRequest()
            .channel(TokenizationChannel.toPaymentsRequestChannelEnum(request.channel))
            .shopperIP(endUserIp ?: "1.1.1.1")
            .paymentMethod((request.paymentMethodDetails as HedvigPaymentMethodDetails).toDefaultPaymentMethodDetails())
            .amount(Amount().value(0L).currency(adyenMerchantInfo.currencyCode.name))
            .merchantAccount(adyenMerchantInfo.account)
            .recurringProcessingModel(RecurringProcessingModelEnum.SUBSCRIPTION)
            .reference(adyenTokenId.toString())
            .returnUrl(request.returnUrl)
            .shopperInteraction(PaymentsRequest.ShopperInteractionEnum.ECOMMERCE)
            .shopperReference(shopperReference)
            .storePaymentMethod(true)

        val browserInfo =
            if (request.browserInfo != null) BrowserInfo.toAdyenBrowserInfo(request.browserInfo) else AdyenBrowserInfo()

        paymentsRequest.browserInfo(browserInfo)

        if (isSubscription) {
            paymentsRequest.recurringProcessingModel(RecurringProcessingModelEnum.SUBSCRIPTION)
        }

        val additionalData: MutableMap<String, String> = HashMap()
        additionalData[ALLOW_3DS2] = allow3DS2.toString()
        paymentsRequest.additionalData = additionalData

        return Pair(adyenTokenId, paymentsRequest)
    }

    override fun submitAdditionalPaymentDetails(
        request: PaymentsDetailsRequest,
        memberId: String
    ): AdyenPaymentsResponse {
        val response = try {
            AdyenPaymentsResponse(paymentsResponse = adyenCheckout.paymentsDetails(request))
        } catch (exception: Exception) {
            logger.error(
                "Submitting additional payment details with Adyen exploded 💥 [MemberId: $memberId] [Request: $request]",
                exception
            )
            throw exception
        }

        val listOfTokenRegistrations = tokenRegistrationRepository.findByMemberId(memberId)

        if (listOfTokenRegistrations.isNullOrEmpty()) {
            throw RuntimeException("Cannot find latest adyen token [MemberId: $memberId]")
        }

        val adyenTokenRegistrationId =
            listOfTokenRegistrations.maxBy(AdyenTokenRegistration::getCreatedAt)!!.adyenTokenRegistrationId

        when (response.getResultCode()) {
            PaymentResponseResultCode.AUTHORISED -> {
                commandGateway.sendAndWait<Void>(
                    AuthorisedAdyenTokenRegistrationCommand(
                        memberId = memberId,
                        adyenTokenRegistrationId = adyenTokenRegistrationId,
                        adyenPaymentsResponse = response,
                        shopperReference = memberId
                    )
                )
            }
            PaymentResponseResultCode.PENDING -> {
                commandGateway.sendAndWait<Void>(
                    UpdatePendingAdyenTokenRegistrationCommand(
                        memberId = memberId,
                        adyenTokenRegistrationId = adyenTokenRegistrationId,
                        adyenPaymentsResponse = response
                    )
                )
            }
            PaymentResponseResultCode.FAILED -> {
                commandGateway.sendAndWait<Void>(
                    CancelAdyenTokenRegistrationCommand(
                        memberId = memberId,
                        adyenTokenRegistrationId = adyenTokenRegistrationId,
                        adyenPaymentsResponse = response
                    )
                )
            }
        }
        return response
    }

    // Extra method for web
    override fun submitAdyenRedirection(
        request: SubmitAdyenRedirectionRequest,
        memberId: String
    ): SubmitAdyenRedirectionResponse {
        val listOfTokenRegistrations = tokenRegistrationRepository.findByMemberId(memberId)

        if (listOfTokenRegistrations.isNullOrEmpty()) {
            throw RuntimeException("Cannot find latest adyen token [MemberId: $memberId]")
        }
        val adyenTokenRegistration = listOfTokenRegistrations.maxBy(AdyenTokenRegistration::getCreatedAt)!!

        require(adyenTokenRegistration.paymentDataFromAction != null) { "No payment data found! [MemberId: $memberId] [Req: $request] " }

        val paymentsDetailsRequest = PaymentsDetailsRequest()
        paymentsDetailsRequest.paymentData = adyenTokenRegistration.paymentDataFromAction

        val details: MutableMap<String, String> = HashMap()
        details[MD] = request.md
        details[PARES] = request.pares
        paymentsDetailsRequest.details = details

        val response = this.submitAdditionalPaymentDetails(paymentsDetailsRequest, memberId)

        return SubmitAdyenRedirectionResponse(resultCode = response.paymentsResponse.resultCode.value)
    }

    override fun fetchAdyenPublicKey(): String {
        return adyenPublicKey
    }

    override fun handleSettlementErrorNotification(adyenTransactionId: UUID) {
        val transaction: AdyenTransaction = transactionRepository.findById(adyenTransactionId).orElseThrow()

        getAndApplyCommand {
            ReceiveCaptureFailureAdyenTransactionCommand(
                transaction.transactionId,
                transaction.memberId
            )
        }
    }

    override fun handleAuthorisationNotification(adyenNotification: NotificationRequestItem) {
        val transactionId = UUID.fromString(adyenNotification.merchantReference!!)

        val payinTransactionMaybe: Optional<AdyenTransaction> = transactionRepository.findById(transactionId)

        val tokenRegistrationMaybe: Optional<AdyenTokenRegistration> = tokenRegistrationRepository.findById(transactionId)

        getAndApplyCommand {
            when {
                payinTransactionMaybe.isPresent -> handlePayinAuthorizationNotification(
                    adyenNotification,
                    payinTransactionMaybe.get()
                )
                tokenRegistrationMaybe.isPresent -> handleTokenRegistration(
                    tokenRegistrationMaybe.get(),
                    adyenNotification
                )
                else -> throw RuntimeException("Handle Authorisation -  Could find not Adyen transaction $transactionId")
            }
        }
    }

    override fun handleRecurringContractNotification(adyenNotification: NotificationRequestItem) {
        val adyenTokenRegistrationId = UUID.fromString(adyenNotification.originalReference)

        val tokenRegistrationMaybe = tokenRegistrationRepository.findById(adyenTokenRegistrationId)

        if (!tokenRegistrationMaybe.isPresent) {
            logger.info("Handle token registration - Could not find adyen token registration $adyenTokenRegistrationId")
            return
        }

        val tokenRegistration = tokenRegistrationMaybe.get()

        if (adyenNotification.success) {
            getAndApplyCommand {
                AuthoriseAdyenTokenRegistrationFromNotificationCommand(
                    adyenTokenRegistrationId = adyenTokenRegistrationId,
                    memberId = tokenRegistration.memberId,
                    adyenNotification = adyenNotification,
                    shopperReference = tokenRegistration.shopperReference
                )
            }
        } else {
            // TODO: Figure out what to do if it is not successful. Maybe keeping it in pending state is okay, maybe we should cancel it
        }
    }

    override fun chargeMemberWithToken(request: ChargeMemberWithTokenRequest): PaymentsResponse {
        val memberAdyenAccount = memberAdyenAccountRepository.findById(request.memberId).orElse(null)
            ?: throw RuntimeException("ChargeMemberWithToken - Member ${request.memberId} doesn't exist")

        require(memberAdyenAccount.recurringDetailReference == request.recurringDetailReference) {
            "RecurringDetailReference mismatch [MemberId : ${memberAdyenAccount.memberId}] " +
                "[MemberRecurringDetailReference: ${memberAdyenAccount.recurringDetailReference} " +
                "[RequestRecurringDetailReference: ${request.recurringDetailReference}] ] "
        }

        val adyenMerchantInfo = adyenMerchantPicker.getAdyenMerchantInfo(request.memberId)

        val additionalData = mutableMapOf(
            "autoRescue" to "true",
            "maxDaysToRescue" to "10"
        )
        if (autoRescueScenario != null) {
            additionalData["autoRescueScenario"] = autoRescueScenario!!
        }

        val paymentsRequest = PaymentsRequest()
            .amount(
                Amount()
                    .value(request.amount.number.longValueExact() * 100)
                    .currency(request.amount.currency.currencyCode)
            )
            .merchantAccount(adyenMerchantInfo.account)
            .paymentMethod(
                DefaultPaymentMethodDetails()
                    .type(ApiConstants.PaymentMethodType.TYPE_SCHEME)
                    .recurringDetailReference(request.recurringDetailReference)
            )
            .recurringProcessingModel(RecurringProcessingModelEnum.SUBSCRIPTION)
            .reference(request.transactionId.toString())
            .shopperInteraction(PaymentsRequest.ShopperInteractionEnum.CONTAUTH)
            .shopperReference(request.memberId)
            .additionalData(additionalData)

        val paymentsResponse: PaymentsResponse

        try {
            paymentsResponse = adyenCheckout.payments(paymentsRequest)
        } catch (exception: Exception) {
            logger.error(
                "Tokenization with Adyen exploded 💥 [MemberId: ${request.memberId}] [Request: $request]",
                exception
            )
            throw exception
        }

        return paymentsResponse
    }

    override fun getActivePayinMethods(memberId: String): ActivePaymentMethodsResponse? {
        val activePaymentMethods = getActivePaymentMethodsResponse(memberId) ?: return null

        val activePaymentMethodWithoutTrustly = excludeTrustlyFromActivePaymentMethods(activePaymentMethods).last()

        return ActivePaymentMethodsResponse(
            storedPaymentMethodsDetails = StoredPaymentMethodsDetails.from(
                activePaymentMethodWithoutTrustly
            )
        )
    }

    override fun getLatestPayoutTokenRegistrationStatus(memberId: String): PayoutMethodStatus? {
        try {
            adyenMerchantPicker.getAdyenMerchantInfo(memberId)
        } catch (e: NoMerchantAccountForMarket) {
            return null
        }

        val listOfTokens = tokenRegistrationRepository.findByMemberId(memberId)

        val lastTokenization = listOfTokens
            .filter { it.isForPayout == true }
            .maxByOrNull { it.createdAt } ?: return PayoutMethodStatus.NEEDS_SETUP

        return PayoutMethodStatus.from(lastTokenization.tokenStatus)
    }

    override fun startPayoutTransaction(
        memberId: String,
        payoutReference: String,
        amount: MonetaryAmount,
        shopperReference: String,
        shopperEmail: String
    ): SubmitResponse {
        val adyenMerchantInfo = adyenMerchantPicker.getAdyenMerchantInfo(memberId)

        val payoutRequest = SubmitRequest()
        payoutRequest.amount = Amount().value(amount.toAdyenMinorUnits()).currency(amount.currency.currencyCode)
        payoutRequest.merchantAccount = adyenMerchantInfo.account
        payoutRequest.recurring = Recurring().contract(Recurring.ContractEnum.PAYOUT)
        payoutRequest.reference = payoutReference
        payoutRequest.shopperEmail = shopperEmail
        payoutRequest.shopperReference = shopperReference
        payoutRequest.selectedRecurringDetailReference = "LATEST"

        return try {
            adyenPayout.submitThirdparty(payoutRequest)
        } catch (exception: Exception) {
            logger.error(
                "StartPayoutTransaction Method exploded 💥 [MemberId: $memberId] [Request: $payoutRequest]",
                exception
            )
            throw exception
        }
    }

    override fun confirmPayout(payoutReference: String, memberId: String): ConfirmThirdPartyResponse {
        val adyenMerchantInfo = adyenMerchantPicker.getAdyenMerchantInfo(memberId)

        val request = ConfirmThirdPartyRequest().also {
            it.merchantAccount = adyenMerchantInfo.account
            it.originalReference = payoutReference
        }
        return try {
            adyenPayoutConfirmation.confirmThirdParty(request)
        } catch (exception: Exception) {
            logger.error("ConfirmPayout Method exploded 💥 [MemberId: $memberId] [Request: $request]", exception)
            throw exception
        }
    }

    override fun handlePayoutThirdPartyNotification(adyenNotification: NotificationRequestItem) =
        getAndApplyCommand {
            getPayoutTransactionCommand(adyenNotification) { transaction ->
                if (adyenNotification.success) {
                    ReceivedSuccessfulAdyenPayoutTransactionFromNotificationCommand(
                        transactionId = transaction.transactionId,
                        memberId = transaction.memberId,
                        amount = Money.of(transaction.amount, transaction.currency)
                    )
                } else {
                    ReceivedFailedAdyenPayoutTransactionFromNotificationCommand(
                        transactionId = transaction.transactionId,
                        memberId = transaction.memberId,
                        amount = Money.of(transaction.amount, transaction.currency),
                        reason = adyenNotification.reason
                    )
                }
            }
        }

    override fun handlePayoutDeclinedNotification(adyenNotification: NotificationRequestItem) =
        getAndApplyCommand {
            getPayoutTransactionCommand(adyenNotification) { transaction ->
                ReceivedDeclinedAdyenPayoutTransactionFromNotificationCommand(
                    transactionId = transaction.transactionId,
                    memberId = transaction.memberId,
                    amount = Money.of(transaction.amount, transaction.currency),
                    reason = adyenNotification.reason
                )
            }
        }

    override fun handlePayoutExpireNotification(adyenNotification: NotificationRequestItem) =
        getAndApplyCommand {
            getPayoutTransactionCommand(adyenNotification) { transaction ->
                ReceivedExpiredAdyenPayoutTransactionFromNotificationCommand(
                    transactionId = transaction.transactionId,
                    memberId = transaction.memberId,
                    amount = Money.of(transaction.amount, transaction.currency),
                    reason = adyenNotification.reason
                )
            }
        }

    override fun handlePayoutPaidOutReservedNotification(adyenNotification: NotificationRequestItem) =
        getAndApplyCommand {
            getPayoutTransactionCommand(adyenNotification) { transaction ->
                ReceivedReservedAdyenPayoutTransactionFromNotificationCommand(
                    transactionId = transaction.transactionId,
                    memberId = transaction.memberId,
                    amount = Money.of(transaction.amount, transaction.currency),
                    reason = adyenNotification.reason
                )
            }
        }

    override fun handleAutoRescueNotification(adyenNotification: NotificationRequestItem): Any =
        withTransaction(adyenNotification) { transaction ->
            ReceivedAdyenTransactionAutoRescueProcessEndedFromNotificationCommand(
                transactionId = transaction.transactionId,
                memberId = transaction.memberId,
                amount = Money.of(transaction.amount, transaction.currency),
                reason = adyenNotification.reason!!,
                rescueReference = adyenNotification.additionalData!!["retry.rescueReference"]!!,
                retryWasSuccessful = adyenNotification.success,
                orderAttemptNumber = adyenNotification.additionalData["retry.orderAttemptNumber"]?.toInt()
            )
        }

    private fun withTransaction(
        adyenNotification: NotificationRequestItem,
        getCommandFromTransaction: (AdyenTransaction) -> Any
    ): Any {
        val adyenTransactionId = UUID.fromString(adyenNotification.merchantReference)

        val transactionMaybe: Optional<AdyenTransaction> = transactionRepository.findById(adyenTransactionId)

        if (!transactionMaybe.isPresent) {
            throw RuntimeException("Handle Authorisation -  Could find not Adyen transaction $adyenTransactionId")
        }

        val transaction = transactionMaybe.get()

        return getCommandFromTransaction(transaction)
    }

    private fun getAndApplyCommand(fn: () -> Any) {
        try {
            val command = fn()
            commandGateway.sendAndWait<Void>(command)
        } catch (e: RuntimeException) {
            logger.error(e.message, e)
        }
    }

    private fun getPayoutTransactionCommand(
        adyenNotification: NotificationRequestItem,
        getCommandFromTransaction: (AdyenPayoutTransaction) -> Any
    ): Any {
        val adyenTransactionId = UUID.fromString(adyenNotification.originalReference)

        val adyenTransactionMaybe = adyenPayoutTransactionRepository.findById(adyenTransactionId)

        if (!adyenTransactionMaybe.isPresent) {
            throw RuntimeException("Handle transaction - Could not find adyen transaction: $adyenTransactionId [adyenNotification: $adyenNotification]")
        }

        val adyenTransaction = adyenTransactionMaybe.get()
        return getCommandFromTransaction(adyenTransaction)
    }

    private fun handleTokenRegistration(tokenRegistration: AdyenTokenRegistration, adyenNotification: NotificationRequestItem) {
        getAndApplyCommand {
            if (adyenNotification.success) {
                AuthoriseAdyenTokenRegistrationFromNotificationCommand(
                    adyenTokenRegistrationId = tokenRegistration.adyenTokenRegistrationId,
                    memberId = tokenRegistration.memberId,
                    adyenNotification = adyenNotification,
                    shopperReference = tokenRegistration.shopperReference
                )
            } else {
                CancelAdyenTokenFromNotificationRegistrationCommand(
                    adyenTokenRegistrationId = tokenRegistration.adyenTokenRegistrationId,
                    memberId = tokenRegistration.memberId
                )
            }
        }
    }

    private fun handlePayinAuthorizationNotification(
        adyenNotification: NotificationRequestItem,
        transaction: AdyenTransaction
    ): Any {
        val hasAutoRescueScheduled = adyenNotification.additionalData?.get("retry.rescueScheduled") == "true"

        return when {
            adyenNotification.success -> ReceiveAuthorisationAdyenTransactionCommand(
                transactionId = transaction.transactionId,
                memberId = transaction.memberId,
                amount = Money.of(transaction.amount, transaction.currency),
                rescueReference = adyenNotification.additionalData?.get("retry.rescueReference")
            )
            hasAutoRescueScheduled -> ReceiveAdyenTransactionUnsuccessfulRetryResponseCommand(
                transactionId = transaction.transactionId,
                memberId = transaction.memberId,
                reason = adyenNotification.reason ?: "No reason provided",
                rescueReference = adyenNotification.additionalData!!["retry.rescueReference"]!!,
                orderAttemptNumber = adyenNotification.additionalData["retry.orderAttemptNumber"]?.toInt()
            )
            else -> ReceiveCancellationResponseAdyenTransactionCommand(
                transactionId = transaction.transactionId,
                memberId = transaction.memberId,
                reason = adyenNotification.reason ?: "No reason provided"
            )
        }
    }

    private fun createMember(memberId: String) {
        val memberMaybe = memberRepository.findById(memberId)

        if (memberMaybe.isPresent) {
            return
        }
        commandGateway.sendAndWait<Void>(CreateMemberCommand(memberId))
    }

    private fun getAvailablePaymentMethods(memberId: String): PaymentMethodsResponse {
        val adyenMerchantInfo = adyenMerchantPicker.getAdyenMerchantInfo(memberId)

        val paymentMethodsRequest = PaymentMethodsRequest()
            .merchantAccount(adyenMerchantInfo.account)
            .countryCode(adyenMerchantInfo.countryCode.alpha2)
            .channel(PaymentMethodsRequest.ChannelEnum.WEB)

        return try {
            adyenCheckout.paymentMethods(paymentMethodsRequest)
        } catch (exception: Exception) {
            logger.error(
                "Fetching available payment methods with Adyen exploded 💥 [Request: $paymentMethodsRequest]",
                exception
            )
            throw exception
        }
    }

    private fun getActivePaymentMethodsResponse(memberId: String): List<StoredPaymentMethod>? {
        val adyenMerchantInfo = try {
            adyenMerchantPicker.getAdyenMerchantInfo(memberId)
        } catch (e: NoMerchantAccountForMarket) {
            return null
        }

        val paymentMethodsRequest = PaymentMethodsRequest()
            .merchantAccount(adyenMerchantInfo.account)
            .shopperReference(memberId)

        val adyenResponse = try {
            adyenCheckout.paymentMethods(paymentMethodsRequest)
        } catch (ex: Exception) {
            logger.error("Active Payment Methods exploded 💥 [MemberId: $memberId] [Request: $paymentMethodsRequest] [Exception: $ex]")
            throw ex
        }

        if (adyenResponse.storedPaymentMethods == null || adyenResponse.storedPaymentMethods.isEmpty()) {
            return null
        }

        return adyenResponse.storedPaymentMethods
    }

    private fun includeOnlyTrustlyFromAvailablePayoutMethods(listOfAvailablePayoutMethods: List<PaymentMethod>): List<PaymentMethod> =
        listOfAvailablePayoutMethods.filter { it.type.toLowerCase() == TRUSTLY }

    private fun excludeTrustlyFromAvailablePayoutMethods(listOfAvailablePayoutMethods: List<PaymentMethod>): List<PaymentMethod> =
        listOfAvailablePayoutMethods.filter { it.type.toLowerCase() != TRUSTLY }

    private fun excludeTrustlyFromAvailablePaymentMethods(listOfAvailablePaymentMethods: List<PaymentMethod>): List<PaymentMethod> =
        listOfAvailablePaymentMethods.filter { it.type.toLowerCase() != TRUSTLY }

    private fun includeOnlyTrustlyFromActivePayoutMethods(listOfAvailablePayoutMethods: List<StoredPaymentMethod>): List<StoredPaymentMethod> =
        listOfAvailablePayoutMethods.filter { it.type.toLowerCase() == TRUSTLY }

    private fun excludeTrustlyFromActivePaymentMethods(listOfAvailablePaymentMethods: List<StoredPaymentMethod>): List<StoredPaymentMethod> =
        listOfAvailablePaymentMethods.filter { it.type.toLowerCase() != TRUSTLY }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)!!
        const val ALLOW_3DS2: String = "allow3DS2"
        const val MD: String = "MD"
        const val PARES: String = "PaRes"
        const val TRUSTLY: String = "trustly"
    }
}
