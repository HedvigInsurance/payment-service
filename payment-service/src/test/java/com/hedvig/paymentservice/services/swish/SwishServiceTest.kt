package com.hedvig.paymentservice.services.swish

import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.paymentservice.services.swish.client.SwishClient
import com.hedvig.paymentservice.services.swish.config.SwishConfigurationProperties
import com.hedvig.paymentservice.services.swish.dto.PayoutRequest
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.javamoney.moneta.Money
import org.junit.jupiter.api.Test
import org.springframework.http.ResponseEntity
import java.time.LocalDateTime
import java.util.UUID

class SwishServiceTest {

    val client: SwishClient = mockk()
    val objectMapper = ObjectMapper()
    val properties = SwishConfigurationProperties().also {
        it.payerAlias = "payerAlias"
        it.tlsCertPath = "/path"
        it.tlsCertPassword = "test"
        it.signingPrivatePemPath = this::class.java.classLoader.getResource("swish/test.pem").path
        it.signingCertificateSerialNumber = "serialNumber"
        it.callbackUrl = "http://callback.url"
    }

    val sut = SwishService(
        objectMapper,
        client,
        properties
    )

    @Test
    fun `start payout should sign and call swish`() {
        val slot = CapturingSlot<PayoutRequest>()
        every { client.payout(capture(slot)) } returns ResponseEntity.ok("")

        sut.startPayout(
            transactionId = transactionId,
            memberId = memberId,
            payeeAlias = payeeAlias,
            payeeSSN = payeeSSN,
            amount = amount,
            message = message,
            instructionDate = LocalDateTime.of(2021, 3, 29, 10, 14),
        )

        val payoutRequest = slot.captured
        assertThat(payoutRequest.callbackUrl).isEqualTo("http://callback.url")
        assertThat(payoutRequest.signature).isNotBlank()
        assertThat(payoutRequest.payload.amount).isEqualTo("12.00")
        assertThat(payoutRequest.payload.currency).isEqualTo("SEK")
        assertThat(payoutRequest.payload.payeeAlias).isEqualTo(payeeAlias)
        assertThat(payoutRequest.payload.payeeSSN).isEqualTo(payeeSSN)
        assertThat(payoutRequest.payload.message).isEqualTo(message)
        assertThat(payoutRequest.payload.instructionDate).isEqualTo("2021-03-29T10:14:00Z")
    }

    companion object {
        val transactionId = UUID.randomUUID()
        val memberId = "1234"
        val payeeAlias = "4670XXX"
        val payeeSSN = "191212121212"
        val amount = Money.of(12, "SEK")
        val message = "message"
    }
}
