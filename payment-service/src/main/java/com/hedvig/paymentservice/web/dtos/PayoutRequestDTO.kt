package com.hedvig.paymentservice.web.dtos

import javax.money.MonetaryAmount

data class PayoutRequestDTO(
    val amount: MonetaryAmount,
    val sanctionBypassed: Boolean
)
