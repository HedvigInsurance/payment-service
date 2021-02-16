package com.hedvig.paymentservice.services.swish

import com.fasterxml.jackson.databind.ObjectMapper
import feign.Client
import org.apache.http.conn.ssl.DefaultHostnameVerifier
import org.apache.http.ssl.SSLContexts
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.io.FileReader
import java.math.BigDecimal
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.Signature
import java.time.LocalDateTime
import java.util.Base64
import java.util.UUID
import javax.net.ssl.SSLSocketFactory

@Configuration
@ConfigurationProperties(prefix = "swish")
class SwishConfigurationProperties {
    lateinit var tlsCertPath: String
    lateinit var tlsCertPassword: String
    lateinit var signingPrivatePemPath: String
    lateinit var signingCertificateSerialNumber: String
    lateinit var callbackUrl: String
}

@Service
class SwishService(
    val objectMapper: ObjectMapper,
    val client: SwishClient,
    val properties : SwishConfigurationProperties
) {
    fun startPayout(
        payerPaymentReference: String,
        payeeAlias: String,
        payeeSSN: String,
        amount: BigDecimal,
        message: String,
        instructionDate: LocalDateTime,
    ) {
        val payload = PayoutPayload(
            UUID.randomUUID().toString().replace("-", "").toUpperCase(),
            payerPaymentReference,
            payeeAlias,
            payeeSSN,
            String.format("%.2f", amount),
            message,
            instructionDate.toString(),
            properties.signingCertificateSerialNumber
        )
        val json = objectMapper.writeValueAsString(payload)
        val signature = SwishSignature.createSignature(json, properties.signingPrivatePemPath)
        val req = PayoutRequest(payload, signature, properties.callbackUrl)
        val response = try {
            client.payout(req)
        } catch (e: Exception) {
            log.error(e.message)
            throw e
        }
        log.info(response.toString())
    }

    data class PayoutRequest(
        val payload: PayoutPayload,
        val signature: String,
        val callbackUrl: String
    )

    data class PayoutPayload(
        val payoutInstructionUUID: String,
        val payerPaymentReference: String,
        val payeeAlias: String,
        val payeeSSN: String,
        val amount: String,
        val message: String,
        val instructionDate: String,
        val signingCertificateSerialNumber: String
    ) {
        val payerAlias: String = "1235261086"
        val currency: String = "SEK"
        val payoutType: String = "PAYOUT"
    }

    private val log: Logger = LoggerFactory.getLogger(this::class.java)

}

object SwishSignature {

    fun createSignature(payload: String, signingPrivatePemPath: String): String {
        val msgBytes = payload.toByteArray()
        val md = MessageDigest.getInstance("SHA-512")
        val hashValue = md.digest(msgBytes);

        val sig = Signature.getInstance("NONEwithRSA")
        val privateKey = loadPrivateKey(signingPrivatePemPath)
        sig.initSign(privateKey)
        sig.update(hashValue)
        val signatureBytes = sig.sign()
        return Base64.getEncoder().encodeToString(signatureBytes)
    }

    fun loadPrivateKey(signingPrivatePemPath: String): PrivateKey {
        val pemParser = PEMParser(FileReader(File(signingPrivatePemPath)))
        val privateKeyInfoAny = pemParser.readObject()
        val converter = JcaPEMKeyConverter()

        return converter.getPrivateKey(privateKeyInfoAny as PrivateKeyInfo)
    }

}

@RestController
@RequestMapping(path = ["/swish/"])
class SwishController(
    val swishService: SwishService
) {

    @GetMapping("payout")
    fun payout() {
        swishService.startPayout(
            payerPaymentReference = "payerPaymentReference",
            payeeAlias = "46726738711",
            payeeSSN = "198607209882",
            amount = BigDecimal.TEN,
            message = "message",
            instructionDate = LocalDateTime.now(),
        )
    }

    @PostMapping("callback")
    fun callback(callback: Callback) {
        log.info(callback.toString())
    }

    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    data class Callback(
        val payoutInstructionUUID: String,
        val payerPaymentReference: String,
        val callbackUrl: String,
        val payerAlias: String,
        val payeeAlias: String,
        val payeeSSN: String,
        val amount: String,
        val currency: String,
        val message: String,
        val status: String,
        val payoutType: String,
        val dateCreated: LocalDateTime,
        val datePaid: LocalDateTime,
        val errorCode: String?,
        val errorMessage: String?,
        val additionalInformation: String?
    )
}

@FeignClient(
    name = "swishClient",
    url = "\${hedvig.external.swish.baseurl:https://staging.getswish.pub.tds.tieto.com/cpc-swish/}",
    configuration = [SwishFeignConfiguration::class]
)
interface SwishClient {
    @PostMapping("/api/v1/payouts/")
    fun payout(
        @RequestBody payloadRequest: SwishService.PayoutRequest
    ): ResponseEntity<*>
}

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
        val sslContext = SSLContexts
            .custom()
            .loadKeyMaterial(File(properties.tlsCertPath), properties.tlsCertPassword.toCharArray(), properties.tlsCertPassword.toCharArray())
            .build()
        sslContext.socketFactory
    } catch (exception: Exception) {
        log.error("Failed to set up swish TLS")
        null
    }
}
