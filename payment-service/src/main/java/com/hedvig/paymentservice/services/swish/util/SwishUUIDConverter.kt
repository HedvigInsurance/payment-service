package com.hedvig.paymentservice.services.swish.util

import java.util.UUID

object SwishUUIDConverter {

    fun fromPayoutInstructionUUIDToTransactionId(payoutInstructionUUID: String): UUID =
        UUID.fromString("${payoutInstructionUUID.substring(0, 8)}-${payoutInstructionUUID.substring(8, 12)}-${payoutInstructionUUID.substring(12, 16)}-${payoutInstructionUUID.substring(16, 20)}-${payoutInstructionUUID.substring(20)}")

    fun fromTransactionIdToPayoutInstructionUUID(transactionId: UUID) =
        transactionId.toString().replace("-", "").toUpperCase()
}
