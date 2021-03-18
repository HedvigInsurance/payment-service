package com.hedvig.paymentservice.domain.payments.events

sealed class PayoutDetails {
    data class Trustly(
        val accountId: String
    ) : PayoutDetails()

    data class Adyen(
        val shopperReference: String
    ) : PayoutDetails()

    data class Swish(
        val phoneNumber: String,
        val ssn: String,
        val message: String
    ) : PayoutDetails()
}
