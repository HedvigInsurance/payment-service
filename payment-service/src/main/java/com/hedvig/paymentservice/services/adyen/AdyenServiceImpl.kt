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
import com.adyen.model.payout.PayoutRequest
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
import com.hedvig.paymentservice.domain.payments.commands.CreateMemberCommand
import com.hedvig.paymentservice.domain.trustlyOrder.commands.AdyenPayoutResponseReceivedCommand
import com.hedvig.paymentservice.graphQl.types.ActivePaymentMethodsResponse
import com.hedvig.paymentservice.graphQl.types.AvailablePaymentMethodsResponse
import com.hedvig.paymentservice.graphQl.types.BrowserInfo
import com.hedvig.paymentservice.graphQl.types.SubmitAdyenRedirectionRequest
import com.hedvig.paymentservice.graphQl.types.SubmitAdyenRedirectionResponse
import com.hedvig.paymentservice.graphQl.types.TokenizationChannel
import com.hedvig.paymentservice.graphQl.types.TokenizationRequest
import com.hedvig.paymentservice.query.adyenTokenRegistration.entities.AdyenTokenRegistration
import com.hedvig.paymentservice.query.adyenTokenRegistration.entities.AdyenTokenRegistrationRepository
import com.hedvig.paymentservice.query.adyenTransaction.entities.AdyenTransaction
import com.hedvig.paymentservice.query.adyenTransaction.entities.AdyenTransactionRepository
import com.hedvig.paymentservice.query.member.entities.MemberRepository
import com.hedvig.paymentservice.serviceIntergration.memberService.MemberService
import com.hedvig.paymentservice.services.adyen.dtos.AdyenPaymentsResponse
import com.hedvig.paymentservice.services.adyen.dtos.ChargeMemberWithTokenRequest
import com.hedvig.paymentservice.services.adyen.dtos.HedvigPaymentMethodDetails
import com.hedvig.paymentservice.services.adyen.dtos.PaymentResponseResultCode
import com.hedvig.paymentservice.services.adyen.dtos.StoredPaymentMethodsDetails
import com.hedvig.paymentservice.web.dtos.adyen.NotificationRequestItem
import org.axonframework.commandhandling.gateway.CommandGateway
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Optional
import java.util.UUID
import javax.money.MonetaryAmount
import com.adyen.model.BrowserInfo as AdyenBrowserInfo

@Service
class AdyenServiceImpl(
  val adyenCheckout: Checkout,
  val adyenPayout: Payout,
  val memberRepository: MemberRepository,
  val uuidGenerator: UUIDGenerator,
  val memberService: MemberService,
  val commandGateway: CommandGateway,
  val adyenTokenRegistrationRepository: AdyenTokenRegistrationRepository,
  val adyenTransactionRepository: AdyenTransactionRepository,
  @param:Value("\${hedvig.adyen.merchantAccount}")
  val merchantAccount: String,
  @param:Value("\${hedvig.adyen.returnUrl}")
  val returnUrl: String,
  @param:Value("\${hedvig.adyen.allow3DS2}")
  val allow3DS2: Boolean,
  @param:Value("\${hedvig.adyen.public.key}")
  val adyenPublicKey: String
) : AdyenService {
  override fun getAvailablePaymentMethods(): AvailablePaymentMethodsResponse {
    val paymentMethodsRequest = PaymentMethodsRequest()
      .merchantAccount(merchantAccount)
      .countryCode("NO") //TODO: Change me by checking the contract
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
    val amount = Amount().value(0).currency("NOK") //TODO: change me by checking the contract
    val (adyenTokenId, paymentsRequest) = createTokenizePaymentsRequest(
      request = req,
      memberId = memberId,
      endUserIp = endUserIp,
      amount = amount,
      shopperReference = memberId
    )

    val response = preformCheckout(paymentsRequest, memberId, req)

    when (response.getResultCode()) {
      PaymentResponseResultCode.AUTHORISED -> {
        commandGateway.sendAndWait<Void>(
          CreateAuthorisedAdyenTokenRegistrationCommand(
            memberId = memberId,
            adyenTokenRegistrationId = adyenTokenId,
            adyenPaymentsResponse = response,
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
    val amount = Amount().value(30).currency("NOK") //TODO: change me by checking the contract
    val shopperReference = "payout_$memberId"
    val (adyenTokenId, paymentsRequest) = createTokenizePaymentsRequest(
      request = request,
      memberId = memberId,
      endUserIp = endUserIp,
      amount = amount,
      shopperReference = shopperReference
    )

    paymentsRequest.enablePayOut(true)

    val response = preformCheckout(paymentsRequest, memberId, request)

    when (response.getResultCode()) {
      PaymentResponseResultCode.AUTHORISED -> {
        commandGateway.sendAndWait<Void>(
          CreateAuthorisedAdyenTokenRegistrationCommand(
            memberId = memberId,
            adyenTokenRegistrationId = adyenTokenId,
            adyenPaymentsResponse = response,
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

  private fun preformCheckout(paymentsRequest: PaymentsRequest, memberId: String, req: TokenizationRequest) = try {
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
    amount: Amount,
    shopperReference: String
  ): Pair<UUID, PaymentsRequest> {
//    val optionalMember = memberService.getMember(memberId)
//    require(optionalMember.isPresent) { "Member not found" }

    createMember(memberId)

    val adyenTokenId = uuidGenerator.generateRandom()

    val paymentsRequest = PaymentsRequest()
      .channel(TokenizationChannel.toPaymentsRequestChannelEnum(request.channel))
      .shopperIP(endUserIp ?: "1.1.1.1")
      .paymentMethod((request.paymentMethodDetails as HedvigPaymentMethodDetails).toDefaultPaymentMethodDetails())
      .amount(amount)
      .merchantAccount(merchantAccount)
      .recurringProcessingModel(RecurringProcessingModelEnum.SUBSCRIPTION)
      .reference(adyenTokenId.toString())
      .returnUrl(request.returnUrl)
      .shopperInteraction(PaymentsRequest.ShopperInteractionEnum.ECOMMERCE)
      .shopperReference(shopperReference)
      .storePaymentMethod(true)

    val browserInfo = if (request.browerInfo != null) BrowserInfo.toAdyenBrowserInfo(request.browerInfo) else AdyenBrowserInfo()

    paymentsRequest.browserInfo(browserInfo)

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

    val paymentsRequest = PaymentsRequest()
      .amount(
        Amount()
          .value(req.amount.number.longValueExact() * 100)
          .currency(req.amount.currency.currencyCode)
      )
      .merchantAccount(merchantAccount)
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
    val paymentMethodsRequest = PaymentMethodsRequest()
      .merchantAccount(merchantAccount)
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

  override fun startPayoutOrder(payoutReference: String, amount: MonetaryAmount, shopperReference: String, shopperEmail: String, hedvigOrderId: UUID) {
    val payoutRequest = PayoutRequest()
      .amount(
        Amount().value(amount.toAdyenMinorUnits()).currency(amount.currency.currencyCode)
      )
      .merchantAccount(merchantAccount)
      .recurring(
        Recurring().contract(Recurring.ContractEnum.PAYOUT)
      )
      .reference(payoutReference)
      .shopperEmail(shopperEmail)
      .shopperReference(shopperReference)
      .selectedRecurringDetailReference("LATEST")


    val payoutResponse = adyenPayout.payout(payoutRequest)

    commandGateway.sendAndWait<Any>(
      AdyenPayoutResponseReceivedCommand(
        hedvigOrderId,
        payoutResponse.pspReference,
        amount
      )
    )
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
