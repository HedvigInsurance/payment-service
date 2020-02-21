package com.hedvig.paymentservice.configuration

import com.adyen.Client
import com.adyen.enums.Environment
import com.adyen.service.Checkout
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AdyenCheckoutConfig {
  @Value("\${hedvig.adyen.apiKey:test}")
  lateinit var apiKey: String

  @Value("\${hedvig.adyen.enviroment:TEST}")
  lateinit var environment: Environment

  @Bean
  fun createAdyenCheckout(): Checkout {
    val client = Client(apiKey, environment)
    return Checkout(client)
  }
}
