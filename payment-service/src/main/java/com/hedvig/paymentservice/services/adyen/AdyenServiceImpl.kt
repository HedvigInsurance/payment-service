package com.hedvig.paymentservice.services.adyen

import com.adyen.constants.ApiConstants
import com.adyen.model.Amount
import com.adyen.model.checkout.DefaultPaymentMethodDetails
import com.adyen.model.checkout.PaymentMethodsRequest
import com.adyen.model.checkout.PaymentMethodsResponse
import com.adyen.model.checkout.PaymentsRequest
import com.adyen.model.checkout.PaymentsRequest.RecurringProcessingModelEnum
import com.adyen.service.Checkout
import com.hedvig.paymentservice.common.UUIDGenerator
import com.hedvig.paymentservice.graphQl.types.TokenizationRequest
import com.hedvig.paymentservice.graphQl.types.TokenizationResponse
import com.hedvig.paymentservice.query.member.entities.MemberRepository
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AdyenServiceImpl(
  val adyenCheckout: Checkout,
  val memberRepository: MemberRepository,
  val uuidGenerator: UUIDGenerator,
  @param:Value("\${hedvig.adyen.merchantAccount:HedvigABCOM}") val merchantAccount: String,
  @param:Value("\${hedvig.adyen.returnUrl:URL}") val returnUrl: String
) : AdyenService {
  override fun getAvailablePaymentMethods(): PaymentMethodsResponse {
    val paymentMethodsRequest = PaymentMethodsRequest()
      .merchantAccount(merchantAccount)
      .countryCode("NO")
      .amount(
        Amount()
          .value(10000)
          .currency("NOK")
      )
      .channel(PaymentMethodsRequest.ChannelEnum.WEB)
    return adyenCheckout.paymentMethods(paymentMethodsRequest)
  }

  override fun tokenizePaymentDetails(req: TokenizationRequest, memberId: String): TokenizationResponse {
    val hedvigOrderId = uuidGenerator.generateRandom()

    val paymentsRequest = PaymentsRequest()
      .paymentMethod(req.paymentsRequest.paymentMethod)
      .amount(Amount().value(0L).currency("NOK")) //TODO: change me
      .merchantAccount(merchantAccount)
      .recurringProcessingModel(RecurringProcessingModelEnum.SUBSCRIPTION)
      .reference(hedvigOrderId.toString())
      .returnUrl(returnUrl)
      .shopperInteraction(PaymentsRequest.ShopperInteractionEnum.ECOMMERCE)
      .shopperReference(memberId)
      .storePaymentMethod(true)

    var response: TokenizationResponse? = null
    try {
      response = TokenizationResponse(paymentsResponse = adyenCheckout.payments(paymentsRequest))
    } catch (ex: Exception) {
      logger.error("Tokenization with Adyen exploded 💥 [MemberId: $memberId] [Request: $req] [Exception: $ex]")
      throw ex
    }
    return response!!
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
          .recurringDetailReference("RECURRING_DETAIL_REFERENCE_FROM_TOKEN")
      )
      .recurringProcessingModel(RecurringProcessingModelEnum.SUBSCRIPTION)
      .reference("ORDER_NUMBER")
      .returnUrl(returnUrl)
      .shopperInteraction(PaymentsRequest.ShopperInteractionEnum.CONTAUTH)
      .shopperReference(req.memberId)
      .storePaymentMethod(true)

    return adyenCheckout.payments(paymentsRequest)
  }

  override fun getActivePaymentMethods(memberId: String): PaymentMethodsResponse {
    val paymentMethodsRequest = PaymentMethodsRequest()
      .merchantAccount(merchantAccount)
      .shopperReference(memberId)

    return adyenCheckout.paymentMethods(paymentMethodsRequest)
  }

  companion object {
    val logger = LoggerFactory.getLogger(this::class.java)
  }
}
