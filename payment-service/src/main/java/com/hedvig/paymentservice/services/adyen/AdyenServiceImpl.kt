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
import com.adyen.service.Checkout
import com.hedvig.paymentservice.common.UUIDGenerator
import com.hedvig.paymentservice.domain.adyenTokenRegistration.commands.AuthorisedAdyenTokenRegistrationCommand
import com.hedvig.paymentservice.domain.adyenTokenRegistration.commands.CancelAdyenTokenRegistrationCommand
import com.hedvig.paymentservice.domain.adyenTokenRegistration.commands.CreateAuthorisedAdyenTokenRegistrationCommand
import com.hedvig.paymentservice.domain.adyenTokenRegistration.commands.CreatePendingAdyenTokenRegistrationCommand
import com.hedvig.paymentservice.domain.adyenTokenRegistration.commands.UpdatePendingAdyenTokenRegistrationCommand
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
import com.hedvig.paymentservice.query.member.entities.MemberRepository
import com.hedvig.paymentservice.serviceIntergration.memberService.MemberService
import com.hedvig.paymentservice.services.adyen.dtos.AdyenPaymentsResponse
import com.hedvig.paymentservice.services.adyen.dtos.ChargeMemberWithTokenRequest
import com.hedvig.paymentservice.services.adyen.dtos.HedvigPaymentMethodDetails
import com.hedvig.paymentservice.services.adyen.dtos.PaymentResponseResultCode
import com.hedvig.paymentservice.services.adyen.dtos.StoredPaymentMethodsDetails
import org.axonframework.commandhandling.gateway.CommandGateway
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import com.adyen.model.BrowserInfo as AdyenBrowserInfo


@Service
class AdyenServiceImpl(
  val adyenCheckout: Checkout,
  val memberRepository: MemberRepository,
  val uuidGenerator: UUIDGenerator,
  val memberService: MemberService,
  val commandGateway: CommandGateway,
  val adyenTokenRegistrationRepository: AdyenTokenRegistrationRepository,
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
      //TODO: Add locale
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
    val optionalMember = memberService.getMember(memberId)
    require(optionalMember.isPresent) { "Member not found" }

    createMember(memberId)

    val adyenTokenId = uuidGenerator.generateRandom()

    val paymentsRequest = PaymentsRequest()
      .channel(TokenizationChannel.toPaymentsRequestChannelEnum(req.channel))
      .shopperIP(endUserIp ?: "1.1.1.1")
      .paymentMethod((req.paymentMethodDetails as HedvigPaymentMethodDetails).toDefaultPaymentMethodDetails())
      .amount(Amount().value(0L).currency("NOK")) //TODO: change me by checking the contract
      .merchantAccount(merchantAccount)
      .recurringProcessingModel(RecurringProcessingModelEnum.SUBSCRIPTION)
      .reference(adyenTokenId.toString())
      .returnUrl(req.returnUrl)
      .shopperInteraction(PaymentsRequest.ShopperInteractionEnum.ECOMMERCE)
      .shopperReference(memberId)
      .storePaymentMethod(true)

    val browserInfo = if (req.browerInfo != null) BrowserInfo.toAdyenBrowserInfo(req.browerInfo) else AdyenBrowserInfo()

    paymentsRequest.browserInfo(browserInfo)

    val additionalData: MutableMap<String, String> = HashMap()
    additionalData[ALLOW_3DS2] = allow3DS2.toString()
    paymentsRequest.additionalData = additionalData

    var response: AdyenPaymentsResponse? = null
    try {
      response = AdyenPaymentsResponse(paymentsResponse = adyenCheckout.payments(paymentsRequest))
    } catch (ex: Exception) {
      logger.error("Tokenization with Adyen exploded 💥 [MemberId: $memberId] [Request: $req] [Exception: $ex]")
      throw ex
    }

    when (response.getResultCode()) {
      PaymentResponseResultCode.AUTHORISED -> {
        commandGateway.sendAndWait<Void>(
          CreateAuthorisedAdyenTokenRegistrationCommand(
            memberId = memberId,
            adyenTokenRegistrationId = adyenTokenId,
            adyenPaymentsResponse = response
          )
        )
      }
      PaymentResponseResultCode.PENDING -> {
        commandGateway.sendAndWait<Void>(
          CreatePendingAdyenTokenRegistrationCommand(
            memberId = memberId,
            adyenTokenRegistrationId = adyenTokenId,
            adyenPaymentsResponse = response,
            paymentDataFromAction = response.paymentsResponse.action.paymentData
          )
        )
      }
      PaymentResponseResultCode.FAILED -> {
        logger.error("Tokenizing payment method failed [MemberId: $memberId] [Request: $req] [Response: $response]")
      }
    }
    return response!!
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
            adyenPaymentsResponse = response
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
