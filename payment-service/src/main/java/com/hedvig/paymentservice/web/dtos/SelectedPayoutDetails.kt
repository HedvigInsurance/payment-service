package com.hedvig.paymentservice.web.dtos

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.hedvig.paymentservice.domain.payments.commands.SelectedPayoutDetails as SelectedPayoutDetailsCommand

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = SelectedPayoutDetails.Swish::class, name = "swish"),
    JsonSubTypes.Type(value = SelectedPayoutDetails.NotSelected::class, name = "notSelected"),
)
sealed class SelectedPayoutDetails {

    abstract fun toCommand(): SelectedPayoutDetailsCommand

    data class Swish(
        val phoneNumber: String,
        val ssn: String,
        val message: String
    ) : SelectedPayoutDetails() {
        override fun toCommand() = SelectedPayoutDetailsCommand.Swish(
            phoneNumber,
            ssn,
            message
        )
    }

    object NotSelected : SelectedPayoutDetails() {
        override fun toCommand() = SelectedPayoutDetailsCommand.NotSelected
    }
}
