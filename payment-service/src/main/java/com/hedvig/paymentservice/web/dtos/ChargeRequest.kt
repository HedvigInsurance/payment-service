package com.hedvig.paymentservice.web.dtos

import javax.money.MonetaryAmount

data class ChargeRequest(
    var amount: MonetaryAmount,
    var requestedBy: String
)
