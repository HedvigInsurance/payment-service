package com.hedvig.paymentservice.domain.payments.events

import com.hedvig.paymentservice.domain.payments.commands.UpdateTrustlyAccountCommand
import lombok.Value
import org.axonframework.commandhandling.model.AggregateIdentifier

import java.util.UUID

@Value
class TrustlyAccountUpdatedEvent {
    val memberId: String? = null

    val hedvigOrderId: UUID? = null
    val trustlyAccountId: String? = null

    val address: String? = null
    val bank: String? = null
    val city: String? = null
    val clearingHouse: String? = null
    val descriptor: String? = null
    val lastDigits: String? = null
    val name: String? = null
    val personId: String? = null
    val zipCode: String? = null

    companion object {

        fun fromUpdateTrustlyAccountCmd(
            memberId: String,
            cmd: UpdateTrustlyAccountCommand
        ): TrustlyAccountUpdatedEvent {
            return TrustlyAccountUpdatedEvent(
                memberId,
                cmd.hedvigOrderId,
                cmd.accountId,
                cmd.address,
                cmd.bank,
                cmd.city,
                cmd.clearingHouse,
                cmd.descriptor,
                cmd.lastDigits,
                cmd.name,
                cmd.personId,
                cmd.zipCode
            )
        }
    }

}
