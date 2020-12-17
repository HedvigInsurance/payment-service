package com.hedvig.paymentservice.domain.payments.events

import com.hedvig.paymentservice.domain.payments.commands.UpdateTrustlyAccountCommand
import org.axonframework.commandhandling.model.AggregateIdentifier
import org.axonframework.serialization.Revision
import java.util.UUID

@Revision("1.0")
data class TrustlyAccountCreatedEvent(
  val memberId: String,

  val hedvigOrderId: UUID,
  val trustlyAccountId: String,

  val address: String?,
  val bank: String?,
  val city: String?,
  val clearingHouse: String?,
  val descriptor: String?,
  val lastDigits: String?,
  val name: String?,
  val personId: String?,
  val zipCode: String?
) {

  companion object {
    fun fromUpdateTrustlyAccountCommand(
      memberId: String,
      cmd: UpdateTrustlyAccountCommand
    ): TrustlyAccountCreatedEvent {
      return TrustlyAccountCreatedEvent(
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
