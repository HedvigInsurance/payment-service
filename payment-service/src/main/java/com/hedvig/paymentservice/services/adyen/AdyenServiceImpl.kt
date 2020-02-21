package com.hedvig.paymentservice.services.adyen

import com.adyen.constants.ApiConstants
import com.adyen.model.Amount
import com.adyen.model.checkout.DefaultPaymentMethodDetails
import com.adyen.model.checkout.PaymentMethodsRequest
import com.adyen.model.checkout.PaymentsRequest
import com.adyen.model.checkout.PaymentsRequest.RecurringProcessingModelEnum
import com.adyen.service.Checkout
import org.springframework.stereotype.Service

@Service
class AdyenServiceImpl(
  val adyenCheckout: Checkout
) : AdyenService {

  override fun registerToken(): Any {
    val paymentsRequest = PaymentsRequest()
      .addEncryptedCardData("", "", "", "", "")
      .amount(Amount().value(0L).currency("SEK"))
      .merchantAccount("HEDVIG")
      .recurringProcessingModel(RecurringProcessingModelEnum.SUBSCRIPTION)
      .reference("ORDER_NUMBER")
      .returnUrl("URL")
      .shopperInteraction(PaymentsRequest.ShopperInteractionEnum.ECOMMERCE)
      .shopperReference("MEMBER_ID")
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
