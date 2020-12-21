package com.hedvig.paymentservice.services.adyen

import com.adyen.constants.ApiConstants
import com.adyen.model.Amount
import com.adyen.model.checkout.DefaultPaymentMethodDetails
import com.adyen.model.checkout.PaymentMethod
import com.adyen.model.checkout.PaymentMethodsRequest
import com.adyen.model.checkout.PaymentMethodsResponse
import com.adyen.model.checkout.PaymentsDetailsRequest
import com.adyen.model.checkout.PaymentsRequest
import com.adyen.model.checkout.PaymentsRequest.RecurringProcessingModelEnum
import com.adyen.model.checkout.PaymentsResponse
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
import com.hedvig.paymentservice.graphQl.types.SubmitAdyenRedirectionRequest
import com.hedvig.paymentservice.graphQl.types.SubmitAdyenRedirectionResponse
import com.hedvig.paymentservice.graphQl.types.TokenizationChannel
import com.hedvig.paymentservice.graphQl.types.TokenizationRequest
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
import org.axonframework.commandhandling.gateway.CommandGateway
import org.javamoney.moneta.Money
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Optional
import java.util.UUID
import javax.money.MonetaryAmount
import kotlin.collections.set
import com.adyen.model.BrowserInfo as AdyenBrowserInfo

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
    val adyenTokenRegistrationRepository: AdyenTokenRegistrationRepository,
    val adyenTransactionRepository: AdyenTransactionRepository,
    val adyenPayoutTransactionRepository: AdyenPayoutTransactionRepository,
    val adyenMerchantPicker: AdyenMerchantPicker,
    @param:Value("\${hedvig.adyen.allow3DS2}")
    val allow3DS2: Boolean,
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

        val listOfTokenRegistrations = adyenTokenRegistrationRepository.findByMemberId(memberId)

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

    override fun submitAdyenRedirection(
        request: SubmitAdyenRedirectionRequest,
        memberId: String
    ): SubmitAdyenRedirectionResponse {
        val listOfTokenRegistrations = adyenTokenRegistrationRepository.findByMemberId(memberId)

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
        val transaction: AdyenTransaction = adyenTransactionRepository.findById(adyenTransactionId).orElseThrow()

        commandGateway.sendAndWait<Void>(
            ReceiveCaptureFailureAdyenTransactionCommand(
                transaction.transactionId,
                transaction.memberId
            )
        )
    }

    override fun handleAuthorisationNotification(adyenNotification: NotificationRequestItem) =
        getPayinTransactionAndApplyCommand(adyenNotification) { transaction ->

            val hasAutoRescueScheduled = adyenNotification.additionalData?.get("retry.rescueScheduled") == "true"

            logger.info("adyenNotification=${adyenNotification}")
            logger.info("additionalData=${adyenNotification.additionalData}")

            when {
                adyenNotification.success -> ReceiveAuthorisationAdyenTransactionCommand(
                    transactionId = transaction.transactionId,
                    memberId = transaction.memberId,
                    rescueReference = adyenNotification.additionalData?.get("retry.rescueReference")
                )
                hasAutoRescueScheduled -> ReceiveAdyenTransactionUnsuccessfulRetryResponseCommand(
                    transactionId = transaction.transactionId,
                    memberId = transaction.memberId,
                    reason = adyenNotification.reason ?: "No reason provided",
                    rescueReference = adyenNotification.additionalData!!["retry.rescueReference"]!!,
                    orderAttemptNumber = adyenNotification.additionalData["retry.orderAttemptNumber"]!!.toInt()
                )
                else -> ReceiveCancellationResponseAdyenTransactionCommand(
                    transactionId = transaction.transactionId,
                    memberId = transaction.memberId,
                    reason = adyenNotification.reason ?: "No reason provided"
                )
            }
        }

    override fun handleRecurringContractNotification(adyenNotification: NotificationRequestItem) {
        val adyenTokenRegistrationId = UUID.fromString(adyenNotification.originalReference)

        val tokenRegistrationMaybe = adyenTokenRegistrationRepository.findById(adyenTokenRegistrationId)

        if (!tokenRegistrationMaybe.isPresent) {
            logger.info("Handle token registration - Could not find adyen token registration $adyenTokenRegistrationId")
            return
        }

        val tokenRegistration = tokenRegistrationMaybe.get()

        if (adyenNotification.success) {
            commandGateway.sendAndWait<Void>(
                AuthoriseAdyenTokenRegistrationFromNotificationCommand(
                    adyenTokenRegistrationId = adyenTokenRegistrationId,
                    memberId = tokenRegistration.memberId,
                    adyenNotification = adyenNotification,
                    shopperReference = tokenRegistration.shopperReference
                )
            )
        } else {
            //TODO: Figure out what to do if it is not successful. Maybe keeping it in pending state is okay, maybe we should cancel it
        }
    }

    override fun chargeMemberWithToken(request: ChargeMemberWithTokenRequest): PaymentsResponse {
        val member = memberRepository.findById(request.memberId).orElse(null)
            ?: throw RuntimeException("ChargeMemberWithToken - Member ${request.memberId} doesn't exist")

        require(member.adyenRecurringDetailReference == request.recurringDetailReference)
        {
            "RecurringDetailReference mismatch [MemberId : ${member.id}] " +
                "[MemberRecurringDetailReference: ${member.adyenRecurringDetailReference} " +
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

    override fun getActivePaymentMethods(memberId: String): ActivePaymentMethodsResponse? {
        val adyenMerchantInfo = try {
            adyenMerchantPicker.getAdyenMerchantInfo(memberId)
        } catch (e: NoMerchantAccountForMarket) {
            return null
        }

        val paymentMethodsRequest = PaymentMethodsRequest()
            .merchantAccount(adyenMerchantInfo.account)
            .shopperReference(memberId)

        val adyenResponse: PaymentMethodsResponse
        try {
            adyenResponse = adyenCheckout.paymentMethods(paymentMethodsRequest)
        } catch (exception: Exception) {
            logger.error(
                "Active Payment Methods exploded 💥 [MemberId: $memberId] [Request: $paymentMethodsRequest]",
                exception
            )
            throw exception
        }

        if (adyenResponse.storedPaymentMethods == null || adyenResponse.storedPaymentMethods.isEmpty()) {
            return null
        }

        return ActivePaymentMethodsResponse(
            storedPaymentMethodsDetails = StoredPaymentMethodsDetails.from(adyenResponse.storedPaymentMethods.first())
        )
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
        getPayoutTransactionAndApplyCommand(adyenNotification) { transaction ->
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

    override fun handlePayoutDeclinedNotification(adyenNotification: NotificationRequestItem) =
        getPayoutTransactionAndApplyCommand(adyenNotification) { transaction ->
            ReceivedDeclinedAdyenPayoutTransactionFromNotificationCommand(
                transactionId = transaction.transactionId,
                memberId = transaction.memberId,
                amount = Money.of(transaction.amount, transaction.currency),
                reason = adyenNotification.reason
            )
        }

    override fun handlePayoutExpireNotification(adyenNotification: NotificationRequestItem) =
        getPayoutTransactionAndApplyCommand(adyenNotification) { transaction ->
            ReceivedExpiredAdyenPayoutTransactionFromNotificationCommand(
                transactionId = transaction.transactionId,
                memberId = transaction.memberId,
                amount = Money.of(transaction.amount, transaction.currency),
                reason = adyenNotification.reason
            )
        }

    override fun handlePayoutPaidOutReservedNotification(adyenNotification: NotificationRequestItem) =
        getPayoutTransactionAndApplyCommand(adyenNotification) { transaction ->
            ReceivedReservedAdyenPayoutTransactionFromNotificationCommand(
                transactionId = transaction.transactionId,
                memberId = transaction.memberId,
                amount = Money.of(transaction.amount, transaction.currency),
                reason = adyenNotification.reason
            )
        }

    override fun handleAutoRescueNotification(adyenNotification: NotificationRequestItem) =
        getPayinTransactionAndApplyCommand(adyenNotification) { transaction ->
            ReceivedAdyenTransactionAutoRescueProcessEndedFromNotificationCommand(
                transactionId = transaction.transactionId,
                memberId = transaction.memberId,
                amount = Money.of(transaction.amount, transaction.currency),
                reason = adyenNotification.reason!!,
                rescueReference = adyenNotification.additionalData!!["retry.rescueReference"]!!,
                retryWasSuccessful = adyenNotification.success,
                orderAttemptNumber = adyenNotification.additionalData["retry.orderAttemptNumber"]!!.toInt()
            )
        }

    private fun getPayinTransactionAndApplyCommand(
        adyenNotification: NotificationRequestItem,
        getCommandFromTransaction: (AdyenTransaction) -> Any
    ) {
        val adyenTransactionId = UUID.fromString(adyenNotification.merchantReference)

        val transactionMaybe: Optional<AdyenTransaction> = adyenTransactionRepository.findById(adyenTransactionId)

        if (!transactionMaybe.isPresent) {
            logger.error("Handle Authorisation -  Could find not Adyen transaction $adyenTransactionId")
            return
        }

        val transaction = transactionMaybe.get()

        commandGateway.sendAndWait<Void>(getCommandFromTransaction(transaction))
    }

    private fun getPayoutTransactionAndApplyCommand(
        adyenNotification: NotificationRequestItem,
        getCommandFromTransaction: (AdyenPayoutTransaction) -> Any
    ) {
        val adyenTransactionId = UUID.fromString(adyenNotification.originalReference)

        val adyenTransactionMaybe = adyenPayoutTransactionRepository.findById(adyenTransactionId)

        if (!adyenTransactionMaybe.isPresent) {
            logger.error("Handle transaction - Could not find adyen transaction: $adyenTransactionId [adyenNotification: $adyenNotification]")
            return
        }

        val adyenTransaction = adyenTransactionMaybe.get()
        commandGateway.sendAndWait<Void>(getCommandFromTransaction(adyenTransaction))
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

    private fun includeOnlyTrustlyFromAvailablePayoutMethods(listOfAvailablePayoutMethods: List<PaymentMethod>): List<PaymentMethod> =
        listOfAvailablePayoutMethods.filter { it.type.toLowerCase() == TRUSTLY }

    private fun excludeTrustlyFromAvailablePaymentMethods(listOfAvailablePaymentMethods: List<PaymentMethod>): List<PaymentMethod> =
        listOfAvailablePaymentMethods.filter { it.type.toLowerCase() != TRUSTLY }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)!!
        const val ALLOW_3DS2: String = "allow3DS2"
        const val MD: String = "MD"
        const val PARES: String = "PaRes"
        const val TRUSTLY: String = "trustly"
    }
}
