package com.hedvig.paymentservice.services.trustly.dto

import javax.money.MonetaryAmount

data class PaymentRequest(
    val memberId: String,
    val amount: MonetaryAmount,
    val accountId: String,
    val email: String
)
