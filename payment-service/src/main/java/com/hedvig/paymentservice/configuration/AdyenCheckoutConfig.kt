package com.hedvig.paymentservice.configuration

import com.adyen.Client
import com.adyen.enums.Environment
import com.adyen.service.Checkout
import com.adyen.service.Payout
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AdyenCheckoutConfig(
  @Value("\${hedvig.adyen.apiKey}")
  val apiKey: String,
  @Value("\${hedvig.adyen.enviroment}")
  val environment: Environment,
  @Value("\${hedvig.adyen.urlPrefix}")
  val prefix: String,
  @Value("\${hedvig.adyen.apiKey.payout:AQEzgmDBbd+uOlwd9n6PwjBt9bPPaKhCAINLVnwY7n2viG9Mv+U97pUzneT0w6ZhX2OSrPTzEMFdWw2+5HzctViMSCJMYAc=-jriYoVu2pcIPFY79BY60BliXGd911j7x0owC5JKhrv8=-Kz5}?y9J%%%x{mhD")
  val apiPayoutKey: String,
  @Value("\${hedvig.adyen.apiKey.payout.confirmation")
  val apiPayoutConfirmationKey: String
) {

  val client: Client = getClient(apiKey)

  val payoutClient: Client = getClient(apiPayoutKey)

  val payoutConfirmationClient: Client = getClient(apiPayoutConfirmationKey)

  @Bean
  fun createAdyenCheckout(): Checkout {
    return Checkout(client)
  }

  @Bean(name = ["AdyenPayout"])
  fun createAdyenPayout(): Payout {
    return Payout(payoutClient)
  }

  @Bean(name = ["AdyenPayoutConfirmation"])
  fun createAdyenPayoutConfirmation(): Payout {
    return Payout(payoutConfirmationClient)
  }

  private fun getClient(key: String) = if (environment == Environment.LIVE) {
    Client(key, environment, prefix)
  } else {
    Client(
      key,
      environment
    )
  }
}
