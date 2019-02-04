package com.hedvig.paymentservice.domain.payments.events

import com.hedvig.paymentservice.domain.payments.commands.UpdateTrustlyAccountCommand
import java.util.*

data class TrustlyAccountUpdatedEvent(
  val memberId: String,

  val hedvigOrderId: UUID,
  val trustlyAccountId: String,

  val address: String,
  val bank: String,
  val city: String,
  val clearingHouse: String,
  val descriptor: String,
  val lastDigits: String,
  val name: String,
  val personId: String,
  val zipCode: String

) {
  companion object {
    @JvmStatic
    fun fromUpdateTrustlyAccountCmd(id: String, cmd: UpdateTrustlyAccountCommand): Any {
      return TrustlyAccountUpdatedEvent(
        id,
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
