package com.hedvig.paymentservice.configuration.jackson

import com.adyen.model.checkout.CheckoutPaymentsAction
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.springframework.context.annotation.Configuration

@Configuration
class CheckoutActionTypeSerializer :
  StdSerializer<CheckoutPaymentsAction.CheckoutActionType>(CheckoutPaymentsAction.CheckoutActionType::class.java) {
  override fun serialize(
    enum: CheckoutPaymentsAction.CheckoutActionType,
    jsonGenerator: JsonGenerator,
    provider: SerializerProvider
  ) {
    return jsonGenerator.writeString(enum.value)
  }
}
