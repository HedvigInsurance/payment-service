package com.hedvig.paymentservice.services.adyen

import com.adyen.constants.ApiConstants
import com.adyen.model.Amount
import com.adyen.model.checkout.DefaultPaymentMethodDetails
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
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceiveAuthorisationAdyenTransactionCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceiveCancellationResponseAdyenTransactionCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceiveCaptureFailureAdyenTransactionCommand
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
  @param:Value("\${hedvig.adyen.returnUrl}")
  val returnUrl: String,
  val adyenMerchantPicker: AdyenMerchantPicker,
  @param:Value("\${hedvig.adyen.allow3DS2}")
  val allow3DS2: Boolean,
  @param:Value("\${hedvig.adyen.public.key}")
  val adyenPublicKey: String
) : AdyenService {
  override fun getAvailablePaymentMethods(memberId: String): AvailablePaymentMethodsResponse {
    val adyenMerchantInfo = adyenMerchantPicker.getAdyenMerchantInfo(memberId)

    val paymentMethodsRequest = PaymentMethodsRequest()
      .merchantAccount(adyenMerchantInfo.account)
      .countryCode(adyenMerchantInfo.countryCode.alpha2)
      .channel(PaymentMethodsRequest.ChannelEnum.WEB)

    val response: PaymentMethodsResponse
    try {
      response = adyenCheckout.paymentMethods(paymentMethodsRequest)
    } catch (ex: Exception) {
      logger.error("Tokenization with Adyen exploded 💥 [Request: $paymentMethodsRequest] [Exception: $ex]")
      throw ex
    }
    return AvailablePaymentMethodsResponse(paymentMethodsResponse = response)
  }

  override fun tokenizePaymentDetails(
    req: TokenizationRequest,
    memberId: String,
    endUserIp: String?
  ): AdyenPaymentsResponse {
    val adyenMerchantInfo = adyenMerchantPicker.getAdyenMerchantInfo(memberId)
    val (adyenTokenId, paymentsRequest) = createTokenizePaymentsRequest(
      request = req,
      memberId = memberId,
      endUserIp = endUserIp,
      shopperReference = memberId,
      isSubscription = true
    )

    val response = performCheckout(paymentsRequest, memberId, req)

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
        logger.error("Tokenizing payment method failed [MemberId: $memberId] [Request: $req] [Response: $response]")
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
  } catch (ex: Exception) {
    logger.error("Tokenization with Adyen exploded 💥 [MemberId: $memberId] [Request: $req] [Exception: $ex]")
    throw ex
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

  override fun submitAdditionalPaymentDetails(req: PaymentsDetailsRequest, memberId: String): AdyenPaymentsResponse {
    var response: AdyenPaymentsResponse? = null
    try {
      response = AdyenPaymentsResponse(paymentsResponse = adyenCheckout.paymentsDetails(req))
    } catch (ex: Exception) {
      logger.error("Submitting additional payment details with Adyen exploded 💥 [MemberId: $memberId] [Request: $req] [Exception: $ex]")
      throw ex
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
    return response!!
  }

  //Extra method for web
  override fun submitAdyenRedirection(
    req: SubmitAdyenRedirectionRequest,
    memberId: String
  ): SubmitAdyenRedirectionResponse {
    val listOfTokenRegistrations = adyenTokenRegistrationRepository.findByMemberId(memberId)

    if (listOfTokenRegistrations.isNullOrEmpty()) {
      throw RuntimeException("Cannot find latest adyen token [MemberId: $memberId]")
    }
    val adyenTokenRegistration = listOfTokenRegistrations.maxBy(AdyenTokenRegistration::getCreatedAt)!!

    require(adyenTokenRegistration.paymentDataFromAction != null) { "No payment data found! [MemberId: $memberId] [Req: $req] " }

    val paymentsDetailsRequest = PaymentsDetailsRequest()
    paymentsDetailsRequest.paymentData = adyenTokenRegistration.paymentDataFromAction

    val details: MutableMap<String, String> = HashMap()
    details[MD] = req.md
    details[PARES] = req.pares
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

  override fun handleAuthorisationNotification(adyenNotification: NotificationRequestItem) {
    val adyenTransactionId = UUID.fromString(adyenNotification.merchantReference!!)

    val transactionMaybe: Optional<AdyenTransaction> = adyenTransactionRepository.findById(adyenTransactionId)

    if (!transactionMaybe.isPresent) {
      logger.info("Handle Authorisation -  Could find Adyen transaction $adyenTransactionId")
      return
    }

    val transaction = transactionMaybe.get()

    if (adyenNotification.success) {
      commandGateway.sendAndWait<Void>(
        ReceiveAuthorisationAdyenTransactionCommand(
          transactionId = transaction.transactionId,
          memberId = transaction.memberId
        )
      )
    } else {
      commandGateway.sendAndWait<Void>(
        ReceiveCancellationResponseAdyenTransactionCommand(
          transactionId = transaction.transactionId,
          memberId = transaction.memberId,
          reason = adyenNotification.reason ?: "No reason provided"
        )
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

  override fun chargeMemberWithToken(req: ChargeMemberWithTokenRequest): PaymentsResponse {
    val member = memberRepository.findById(req.memberId).orElse(null)
      ?: throw RuntimeException("ChargeMemberWithToken - Member ${req.memberId} doesn't exist")

    require(member.adyenRecurringDetailReference == req.recurringDetailReference)
    {
      "RecurringDetailReference mismatch [MemberId : ${member.id}] " +
        "[MemberRecurringDetailReference: ${member.adyenRecurringDetailReference} " +
        "[RequestRecurringDetailReference: ${req.recurringDetailReference}] ] "
    }

    val adyenMerchantInfo = adyenMerchantPicker.getAdyenMerchantInfo(req.memberId)

    val paymentsRequest = PaymentsRequest()
      .amount(
        Amount()
          .value(req.amount.number.longValueExact() * 100)
          .currency(req.amount.currency.currencyCode)
      )
      .merchantAccount(adyenMerchantInfo.account)
      .paymentMethod(
        DefaultPaymentMethodDetails()
          .type(ApiConstants.PaymentMethodType.TYPE_SCHEME)
          .recurringDetailReference(req.recurringDetailReference)
      )
      .recurringProcessingModel(RecurringProcessingModelEnum.SUBSCRIPTION)
      .reference(req.transactionId.toString())
      .shopperInteraction(PaymentsRequest.ShopperInteractionEnum.CONTAUTH)
      .shopperReference(req.memberId)

    val paymentsResponse: PaymentsResponse

    try {
      paymentsResponse = adyenCheckout.payments(paymentsRequest)
    } catch (ex: Exception) {
      logger.error("Tokenization with Adyen exploded 💥 [MemberId: ${req.memberId}] [Request: $req] [Exception: $ex]")
      throw ex
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
    } catch (ex: Exception) {
      logger.error("Active Payment Methods exploded 💥 [MemberId: $memberId] [Request: $paymentMethodsRequest] [Exception: $ex]")
      throw ex
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

    return adyenPayout.submitThirdparty(payoutRequest)
  }

  override fun confirmPayout(payoutReference: String, memberId: String): ConfirmThirdPartyResponse {
    val adyenMerchantInfo = adyenMerchantPicker.getAdyenMerchantInfo(memberId)

    val request = ConfirmThirdPartyRequest().also {
      it.merchantAccount = adyenMerchantInfo.account
      it.originalReference = payoutReference
    }
    return adyenPayoutConfirmation.confirmThirdParty(request)
  }

  override fun handlePayoutThirdPartyNotification(adyenNotification: NotificationRequestItem) =
    getPayoutTransactionAndApplyComand(adyenNotification) { transaction ->
      if (adyenNotification.success) {
        commandGateway.sendAndWait<Void>(
          ReceivedSuccessfulAdyenPayoutTransactionFromNotificationCommand(
            transaction.transactionId,
            transaction.memberId,
            Money.of(transaction.amount, transaction.currency)
          )
        )
      } else {
        commandGateway.sendAndWait<Void>(
          ReceivedFailedAdyenPayoutTransactionFromNotificationCommand(
            transaction.transactionId,
            transaction.memberId,
            Money.of(transaction.amount, transaction.currency),
            adyenNotification.reason
          )
        )
      }
    }

  override fun handlePayoutDeclinedNotification(adyenNotification: NotificationRequestItem) =
    getPayoutTransactionAndApplyComand(adyenNotification) { transaction ->
      commandGateway.sendAndWait<Void>(
        ReceivedDeclinedAdyenPayoutTransactionFromNotificationCommand(
          transaction.transactionId,
          transaction.memberId,
          Money.of(transaction.amount, transaction.currency),
          adyenNotification.reason
        )
      )
    }

  override fun handlePayoutExpireNotification(adyenNotification: NotificationRequestItem) =
    getPayoutTransactionAndApplyComand(adyenNotification) { transaction ->
      commandGateway.sendAndWait<Void>(
        ReceivedExpiredAdyenPayoutTransactionFromNotificationCommand(
          transaction.transactionId,
          transaction.memberId,
          Money.of(transaction.amount, transaction.currency),
          adyenNotification.reason
        )
      )
    }

  override fun handlePayoutPaidOutReservedNotification(adyenNotification: NotificationRequestItem) =
    getPayoutTransactionAndApplyComand(adyenNotification) { transaction ->
      commandGateway.sendAndWait<Void>(
        ReceivedReservedAdyenPayoutTransactionFromNotificationCommand(
          transaction.transactionId,
          transaction.memberId,
          Money.of(transaction.amount, transaction.currency),
          adyenNotification.reason
        )
      )
    }

  private fun getPayoutTransactionAndApplyComand(
    adyenNotification: NotificationRequestItem,
    command: (AdyenPayoutTransaction) -> Unit
  ) {
    val adyenTransactionId = UUID.fromString(adyenNotification.originalReference)

    val adyenTransactionMaybe = adyenPayoutTransactionRepository.findById(adyenTransactionId)

    if (!adyenTransactionMaybe.isPresent) {
      logger.info("Handle transaction - Could not find adyen transaction: $adyenTransactionId [adyenNotification: $adyenNotification]")
      return
    }

    val adyenTransaction = adyenTransactionMaybe.get()
    command(adyenTransaction)
  }

  private fun createMember(memberId: String) {
    val memberMaybe = memberRepository.findById(memberId)

    if (memberMaybe.isPresent) {
      return
    }
    commandGateway.sendAndWait<Void>(CreateMemberCommand(memberId))
  }

  companion object {
    val logger = LoggerFactory.getLogger(this::class.java)
    const val ALLOW_3DS2: String = "allow3DS2"
    const val MD: String = "MD"
    const val PARES: String = "PaRes"
  }
}
