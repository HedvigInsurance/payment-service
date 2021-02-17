package com.hedvig.paymentservice.services.swish.client

import com.hedvig.paymentservice.services.swish.SwishConfigurationProperties
import feign.Client
import org.apache.http.conn.ssl.DefaultHostnameVerifier
import org.apache.http.ssl.SSLContexts
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import java.io.File
import javax.net.ssl.SSLSocketFactory

class SwishFeignConfiguration {

    @Autowired
    lateinit var properties : SwishConfigurationProperties

    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun feignClient(): Client {
        val sslSocketFactory = getSSLSocketFactory()
        return Client.Default(sslSocketFactory, DefaultHostnameVerifier())
    }

    private fun getSSLSocketFactory(): SSLSocketFactory? = try {
        val sslContext = SSLContexts.custom()
            .loadKeyMaterial(File(properties.tlsCertPath), properties.tlsCertPassword.toCharArray(), properties.tlsCertPassword.toCharArray())
            .build()
        sslContext.socketFactory
    } catch (exception: Exception) {
        log.error("Failed to set up swish TLS")
        null
    }
}
