package com.hedvig.paymentservice.domain.payments

data class TrustlyAccount(
    val accountId: String,
    var directDebitStatus: DirectDebitStatus?
)
