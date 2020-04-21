package com.hedvig.paymentservice.serviceIntergration.productPricing.dto

import com.hedvig.paymentservice.query.member.entities.Transaction
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

data class PolicyGuessRequestDto(
  val id: UUID,
  val memberId: String,
  val date: LocalDate
) {

  companion object {
    fun from(transaction: Transaction): PolicyGuessRequestDto {
      return PolicyGuessRequestDto(
        transaction.id,
        transaction.member.getId(),
        transaction.timestamp.atZone(ZoneId.of("Europe/Stockholm")).toLocalDate()
      )
    }
  }
}
