package com.hedvig.paymentservice.services.adyen

import com.adyen.constants.ApiConstants
import com.adyen.model.Amount
import com.adyen.model.checkout.DefaultPaymentMethodDetails
import com.adyen.model.checkout.PaymentMethodsRequest
import com.adyen.model.checkout.PaymentsRequest
import com.adyen.model.checkout.PaymentsRequest.RecurringProcessingModelEnum
import com.adyen.service.Checkout
import com.hedvig.paymentservice.common.UUIDGenerator
import com.hedvig.paymentservice.services.adyen.dtos.CardRegistrationRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AdyenServiceImpl(
  val adyenCheckout: Checkout,
  val uuidGenerator: UUIDGenerator,
  @param:Value("\${hedvig.adyen.merchantAccount:HEDVIG}") val merchantAccount: String,
  @param:Value("\${hedvig.adyen.returnUrl:URL}") val returnUrl: String
) : AdyenService {

  override fun registerToken(req: CardRegistrationRequest): Any {
    val hedvigOrderId = uuidGenerator.generateRandom()

    val paymentsRequest = PaymentsRequest()
      .addEncryptedCardData(
        req.encryptedCardData.encryptedCardNumber,
        req.encryptedCardData.encryptedExpiryMonth,
        req.encryptedCardData.encryptedExpiryYear,
        req.encryptedCardData.encryptedSecurityCode,
        req.encryptedCardData.holderName
      )
      .amount(Amount().value(0L).currency(req.desiredCurrency))
      .merchantAccount(merchantAccount)
      .recurringProcessingModel(RecurringProcessingModelEnum.SUBSCRIPTION)
      .reference(hedvigOrderId.toString())
      .returnUrl(returnUrl)
      .shopperInteraction(PaymentsRequest.ShopperInteractionEnum.ECOMMERCE)
      .shopperReference(req.member.memberId)
      .storePaymentMethod(true)

    return adyenCheckout.payments(paymentsRequest)
  }

  override fun chargeMemberWithToken(): Any {
    val paymentsRequest = PaymentsRequest()
      .amount(Amount().value(1500L).currency("SEK"))
      .merchantAccount("HEDVIG")
      .paymentMethod(
        DefaultPaymentMethodDetails()
          .type(ApiConstants.PaymentMethodType.TYPE_SCHEME)
          .recurringDetailReference("RECURRING_DETAIL_REFERENCE_FROM_TOKEN")
      )
      .recurringProcessingModel(RecurringProcessingModelEnum.SUBSCRIPTION)
      .reference("ORDER_NUMBER")
      .returnUrl("URL")
      .shopperInteraction(PaymentsRequest.ShopperInteractionEnum.CONTAUTH)
      .shopperReference("MEMBER_ID")
      .storePaymentMethod(true)

    return adyenCheckout.payments(paymentsRequest)
  }

  override fun fetchCardDetails(): Any {
    val paymentMethodsRequest = PaymentMethodsRequest()
      .merchantAccount("HEDVIG")
      .shopperReference("MEMBER_ID")

    return adyenCheckout.paymentMethods(paymentMethodsRequest)
  }
}
