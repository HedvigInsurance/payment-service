package com.hedvig.paymentservice.web.dtos

import com.hedvig.paymentservice.domain.payments.DirectDebitStatus
import com.hedvig.paymentservice.query.member.entities.Member
import java.util.UUID

data class PaymentMemberDTO(
    val id: String,
    val transactions: Map<UUID, TransactionDTO>,
    val directDebitMandateActive: Boolean,
    val trustlyAccountNumber: String? = null
) {

  companion object {
    fun fromMember(member: Member, latestDirectDebitAccountOrderDTO: DirectDebitAccountOrderDTO?) = PaymentMemberDTO(
      id = member.getId(),
      transactions = member.transactions.entries.map { entry -> entry.key to TransactionDTO.fromTransaction(entry.value) }.toMap(),
      directDebitMandateActive = latestDirectDebitAccountOrderDTO?.directDebitStatus == DirectDebitStatus.CONNECTED,
      trustlyAccountNumber = latestDirectDebitAccountOrderDTO?.trustlyAccountId
    )
  }
}
