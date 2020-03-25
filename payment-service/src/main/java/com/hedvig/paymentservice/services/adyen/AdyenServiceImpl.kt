package com.hedvig.paymentservice.services.adyen

import com.adyen.constants.ApiConstants
import com.adyen.model.Amount
import com.adyen.model.checkout.DefaultPaymentMethodDetails
import com.adyen.model.checkout.PaymentMethodsRequest
import com.adyen.model.checkout.PaymentsDetailsRequest
import com.adyen.model.checkout.PaymentsRequest
import com.adyen.model.checkout.PaymentsRequest.RecurringProcessingModelEnum
import com.adyen.service.Checkout
import com.hedvig.paymentservice.common.UUIDGenerator
import com.hedvig.paymentservice.domain.adyen.commands.CreateAdyenTokenCommand
import com.hedvig.paymentservice.domain.payments.commands.CreateMemberCommand
import com.hedvig.paymentservice.graphQl.types.ActivePaymentMethodsResponse
import com.hedvig.paymentservice.graphQl.types.AvailablePaymentMethodsResponse
import com.hedvig.paymentservice.graphQl.types.TokenizationRequest
import com.hedvig.paymentservice.query.member.entities.MemberRepository
import com.hedvig.paymentservice.serviceIntergration.memberService.MemberService
import com.hedvig.paymentservice.services.adyen.dtos.AdyenPaymentsResponse
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberRequest
import org.axonframework.commandhandling.gateway.CommandGateway
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AdyenServiceImpl(
  val adyenCheckout: Checkout,
  val memberRepository: MemberRepository,
  val uuidGenerator: UUIDGenerator,
  val memberService: MemberService,
  val commandGateway: CommandGateway,
  @param:Value("\${hedvig.adyen.merchantAccount}") val merchantAccount: String
) : AdyenService {
  override fun getAvailablePaymentMethods(): AvailablePaymentMethodsResponse {
    val paymentMethodsRequest = PaymentMethodsRequest()
      .merchantAccount(merchantAccount)
      .countryCode("NO") //TODO: Change me by checking the contract
      //TODO: Add locale
      .channel(PaymentMethodsRequest.ChannelEnum.WEB)
    return AvailablePaymentMethodsResponse(paymentMethodsResponse = adyenCheckout.paymentMethods(paymentMethodsRequest))
  }

  override fun tokenizePaymentDetails(req: TokenizationRequest, memberId: String): AdyenPaymentsResponse {
    val optionalMember = memberService.getMember(memberId)
    require(optionalMember.isPresent) { "Member not found" }

    createMember(memberId)

    val adyenTokenId = uuidGenerator.generateRandom()

    val paymentsRequest = PaymentsRequest()
      .paymentMethod(req.paymentsRequest.paymentMethod)
      .amount(Amount().value(0L).currency("NOK")) //TODO: change me by checking the contract
      .merchantAccount(merchantAccount)
      .recurringProcessingModel(RecurringProcessingModelEnum.SUBSCRIPTION)
      .reference(adyenTokenId.toString())
      .returnUrl(req.paymentsRequest.returnUrl)
      .shopperInteraction(PaymentsRequest.ShopperInteractionEnum.ECOMMERCE)
      .shopperReference(memberId)
      .storePaymentMethod(true)

    var response: AdyenPaymentsResponse? = null
    try {
      response = AdyenPaymentsResponse(paymentsResponse = adyenCheckout.payments(paymentsRequest))
    } catch (ex: Exception) {
      logger.error("Tokenization with Adyen exploded 💥 [MemberId: $memberId] [Request: $req] [Exception: $ex]")
      throw ex
    }

    commandGateway.sendAndWait<Void>(
      CreateAdyenTokenCommand(
        memberId = memberId,
        adyenTokenId = adyenTokenId,
        tokenizationResponse = response
      )
    )

    //TODO: Cancel rest

    return response!!
  }

  override fun submitAdditionalPaymentDetails(req: PaymentsDetailsRequest): AdyenPaymentsResponse {
    return AdyenPaymentsResponse(paymentsResponse = adyenCheckout.paymentsDetails(req))
  }

  override fun chargeMemberWithToken(req: ChargeMemberRequest): Any {
    val member = memberRepository.findById(req.memberId).orElse(null) ?: TODO("Shall we throw an exception?")

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
          .recurringDetailReference("RECURRING_DETAIL_REFERENCE_FROM_TOKEN") //TODO: CHANGE ME
      )
      .recurringProcessingModel(RecurringProcessingModelEnum.SUBSCRIPTION)
      .reference("ORDER_NUMBER")
      .returnUrl(returnUrl)
      .shopperInteraction(PaymentsRequest.ShopperInteractionEnum.CONTAUTH)
      .shopperReference(req.memberId)
      .storePaymentMethod(true)

    return adyenCheckout.payments(paymentsRequest)
  }

  override fun getActivePaymentMethods(memberId: String): ActivePaymentMethodsResponse {
    val paymentMethodsRequest = PaymentMethodsRequest()
      .merchantAccount(merchantAccount)
      .shopperReference(memberId)
    return ActivePaymentMethodsResponse(adyenCheckout.paymentMethods(paymentMethodsRequest))
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
  }
}
