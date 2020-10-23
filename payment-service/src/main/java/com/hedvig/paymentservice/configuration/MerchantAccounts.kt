package com.hedvig.paymentservice.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("hedvig.adyen")
class MerchantAccounts {
  var merchantAccounts: Map<String, String>? = null
}
