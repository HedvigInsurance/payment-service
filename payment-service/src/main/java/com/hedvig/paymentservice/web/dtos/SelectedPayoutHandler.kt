package com.hedvig.paymentservice.web.dtos

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.hedvig.paymentservice.domain.payments.commands.SelectedPayoutHandler as SelectedPayoutHandlerCommand

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = SelectedPayoutHandler.Swish::class, name = "swish"),
    JsonSubTypes.Type(value = SelectedPayoutHandler.NotSelected::class, name = "notSelected"),
)
sealed class SelectedPayoutHandler {

    abstract fun toCommand(): SelectedPayoutHandlerCommand

    data class Swish(
        val phoneNumber: String,
        val ssn: String,
        val message: String
    ) : SelectedPayoutHandler() {
        override fun toCommand() = SelectedPayoutHandlerCommand.Swish(
            phoneNumber,
            ssn,
            message
        )
    }

    object NotSelected : SelectedPayoutHandler() {
        override fun toCommand() = SelectedPayoutHandlerCommand.NotSelected
    }
}
