package com.hedvig.paymentservice.web.dtos

import java.util.UUID

class FailChargeRequestDTO(
    val memberId: String,
    val transactionId: UUID
)
