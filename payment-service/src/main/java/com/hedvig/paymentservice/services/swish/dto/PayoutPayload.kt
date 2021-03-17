package com.hedvig.paymentservice.services.swish.dto

data class PayoutPayload(
    val payerAlias: String,
    val payoutInstructionUUID: String,
    val payerPaymentReference: String,
    val payeeAlias: String,
    val payeeSSN: String,
    val amount: String,
    val currency: String,
    val message: String,
    val instructionDate: String,
    val signingCertificateSerialNumber: String
) {
    val payoutType: String = "PAYOUT"
}
