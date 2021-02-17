package com.hedvig.paymentservice.domain.payments.commands

sealed class SelectedPayoutHandler {
    data class Swish(
        val phoneNumber: String,
        val ssn: String,
        val message: String
    ): SelectedPayoutHandler()
    
    object NotSelected: SelectedPayoutHandler()
}
