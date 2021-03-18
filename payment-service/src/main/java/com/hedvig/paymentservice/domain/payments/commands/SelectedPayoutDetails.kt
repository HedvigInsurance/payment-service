package com.hedvig.paymentservice.domain.payments.commands

sealed class SelectedPayoutDetails {
    data class Swish(
        val phoneNumber: String,
        val ssn: String,
        val message: String
    ): SelectedPayoutDetails()

    object NotSelected: SelectedPayoutDetails()
}
