package com.hedvig.paymentservice.services.swish

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class SwishUUIDConverterTest {

    @Test
    fun `test fromPayoutInstructionUUIDToTransactionId`() {
        val transactionId = SwishUUIDConverter.fromPayoutInstructionUUIDToTransactionId(
            "8F71ED08712D11EB94390242AC130002"
        )

        assertThat(transactionId).isEqualTo(UUID.fromString("8f71ed08-712d-11eb-9439-0242ac130002"))
    }

    @Test
    fun `test fromTransactionIdToPayoutInstructionUUID`() {
        val payoutInstructionUUID = SwishUUIDConverter.fromTransactionIdToPayoutInstructionUUID(
            UUID.fromString("8f71ed08-712d-11eb-9439-0242ac130002")
        )

        assertThat(payoutInstructionUUID).isEqualTo("8F71ED08712D11EB94390242AC130002")
    }
}
