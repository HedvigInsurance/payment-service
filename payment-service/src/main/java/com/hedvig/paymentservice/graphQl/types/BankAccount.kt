package com.hedvig.paymentservice.graphQl.types

import com.hedvig.paymentservice.query.member.entities.Member


data class BankAccount(
  val bankName: String,
  val descriptor: String,
  val directDebitStatus: DirectDebitStatus
) {
  companion object {
    @kotlin.jvm.JvmStatic
    fun fromMember(m: Member): BankAccount {
      return BankAccount(
        m.bank,
        m.descriptor,
        fromMemberDirectStatus(m.directDebitStatus)
      )
    }

    private fun fromMemberDirectStatus(s: com.hedvig.paymentservice.domain.payments.DirectDebitStatus): DirectDebitStatus {
      return when (s) {
        com.hedvig.paymentservice.domain.payments.DirectDebitStatus.CONNECTED -> DirectDebitStatus.ACTIVE
        com.hedvig.paymentservice.domain.payments.DirectDebitStatus.PENDING -> DirectDebitStatus.PENDING
        com.hedvig.paymentservice.domain.payments.DirectDebitStatus.DISCONNECTED -> DirectDebitStatus.NEEDS_SETUP
      }
    }
  }
}


