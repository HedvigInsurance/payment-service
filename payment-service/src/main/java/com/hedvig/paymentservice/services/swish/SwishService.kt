package com.hedvig.paymentservice.services.swish

import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.paymentservice.services.swish.client.SwishClient
import feign.FeignException
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID
import javax.money.MonetaryAmount

@Service
class SwishService(
    val objectMapper: ObjectMapper,
    val client: SwishClient,
    val properties : SwishConfigurationProperties
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
            payoutInstructionUUID = transactionId.toString().replace("-", "").toUpperCase(),
            payerPaymentReference = memberId,
            payeeAlias = payeeAlias,
            payeeSSN = payeeSSN,
            amount = String.format("%.2f", amount),
            currency = amount.currency.currencyCode,
            message = message,
            instructionDate = instructionDate.toString(),
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

