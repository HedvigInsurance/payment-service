package com.hedvig.paymentservice.web.dtos

data class PayoutMethodStatusDTO(
    val memberId: String,
    val activated: Boolean
)
