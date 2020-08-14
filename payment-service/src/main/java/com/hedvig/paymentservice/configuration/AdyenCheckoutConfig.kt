package com.hedvig.paymentservice.configuration

import com.adyen.Client
import com.adyen.enums.Environment
import com.adyen.service.Checkout
import com.adyen.service.Payout
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AdyenCheckoutConfig {
  @Value("\${hedvig.adyen.apiKey}")
  lateinit var apiKey: String

  @Value("\${hedvig.adyen.enviroment}")
  lateinit var environment: Environment

  @Value("\${hedvig.adyen.urlPrefix}")
  lateinit var prefix: String

  @Bean
  fun createAdyenCheckout(): Checkout {
    val client: Client = if (environment == Environment.LIVE) {
      Client(apiKey, environment, prefix)
    } else {
      Client(apiKey, environment)
    }
    return Checkout(client)
  }

  @Bean
  fun createAdyenPayout(): Payout {
    val client: Client = if (environment == Environment.LIVE) {
      Client(apiKey, environment, prefix)
    } else {
      Client(apiKey, environment)
    }
    return Payout(client)
  }
}
