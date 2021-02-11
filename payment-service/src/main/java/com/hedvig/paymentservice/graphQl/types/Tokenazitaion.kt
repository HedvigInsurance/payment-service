package com.hedvig.paymentservice.graphQl.types

import com.adyen.model.checkout.PaymentsRequest

enum class TokenizationChannel {
  ANDROID,
  IOS,
  WEB;

  companion object {
    fun toPaymentsRequestChannelEnum(enum: TokenizationChannel): PaymentsRequest.ChannelEnum {
      return when (enum) {
        ANDROID -> PaymentsRequest.ChannelEnum.ANDROID
        IOS -> PaymentsRequest.ChannelEnum.IOS
        WEB -> PaymentsRequest.ChannelEnum.WEB
      }
    }
  }
}
