package com.hedvig.paymentservice.services.swish.dto

import java.time.LocalDateTime

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
