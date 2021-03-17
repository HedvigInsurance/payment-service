package com.hedvig.paymentservice.services.swish

import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.paymentservice.services.swish.client.SwishClient
import com.hedvig.paymentservice.services.swish.config.SwishConfigurationProperties
import com.hedvig.paymentservice.services.swish.dto.PayoutPayload
import com.hedvig.paymentservice.services.swish.dto.PayoutRequest
import com.hedvig.paymentservice.services.swish.dto.StartPayoutResponse
import com.hedvig.paymentservice.services.swish.util.SwishSignatureCreator
import com.hedvig.paymentservice.services.swish.util.SwishUUIDConverter
import feign.FeignException
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID
import javax.money.MonetaryAmount

@Service
class SwishService(
    private val objectMapper: ObjectMapper,
    private val client: SwishClient,
    private val properties : SwishConfigurationProperties
) {
    fun startPayout(
        transactionId: UUID,
        memberId: String,
        payeeAlias: String,
        payeeSSN: String,
        amount: MonetaryAmount,
        message: String,
        instructionDate: LocalDateTime,
    ): StartPayoutResponse {
        val payload = PayoutPayload(
            payerAlias = properties.payerAlias,
            payoutInstructionUUID = SwishUUIDConverter.fromTransactionIdToPayoutInstructionUUID(transactionId),
            payerPaymentReference = memberId,
            payeeAlias = payeeAlias,
            payeeSSN = payeeSSN,
            amount = String.format("%.2f", amount.number.doubleValueExact()),
            currency = amount.currency.currencyCode,
            message = message,
            instructionDate = "2021-03-11T13:45:36Z",
            signingCertificateSerialNumber = properties.signingCertificateSerialNumber
        )
        val json = objectMapper.writeValueAsString(payload)
        val signature = SwishSignatureCreator.createSignature(json, properties.signingPrivatePemPath)
        val req = PayoutRequest(payload, signature, properties.callbackUrl)
        return try {
            client.payout(req)
            StartPayoutResponse.Success
        } catch (e: Exception) {
            StartPayoutResponse.Failed(e.message, (e as FeignException?)?.status())
        }
    }
}

