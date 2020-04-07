package com.hedvig.paymentservice.configuration.jackson

import com.adyen.model.checkout.PaymentMethodDetails
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.hedvig.paymentservice.services.adyen.dtos.HedvigPaymentMethodDetails
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.zalando.jackson.datatype.money.MoneyModule

@Configuration
class JacksonConfig {
  @Bean
  fun objectMapper(): ObjectMapper {
    val paymentMethodDetailsModule = SimpleModule("PaymentMethodDetailsModule", Version.unknownVersion())

    paymentMethodDetailsModule.setAbstractTypes(
      SimpleAbstractTypeResolver().addMapping(
        PaymentMethodDetails::class.java,
        HedvigPaymentMethodDetails::class.java
      )
    )

    val serializer = CheckoutActionTypeSerializer()

    val module =
      SimpleModule()
    module.addSerializer(serializer)

    val mapper: ObjectMapper = Jackson2ObjectMapperBuilder.json()
      .modules(
        paymentMethodDetailsModule,
        MoneyModule()
          .withQuotedDecimalNumbers(),
        JavaTimeModule(),
        module
      ).build()

    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)

    return mapper
  }
}
