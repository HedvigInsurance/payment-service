package com.hedvig.paymentservice.services.swish

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "swish")
class SwishConfigurationProperties {
    lateinit var tlsCertPath: String
    lateinit var tlsCertPassword: String
    lateinit var signingPrivatePemPath: String
    lateinit var signingCertificateSerialNumber: String
    lateinit var callbackUrl: String
}
