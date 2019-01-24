package com.hedvig.paymentservice.services.trustly.dto

import java.time.LocalDate
import javax.money.MonetaryAmount

data class PayoutRequest(
  val memberId: String,
  val amount: MonetaryAmount,
  val accountId: String,
  val address: String,
  val countryCode: String,
  val dateOfBirth: LocalDate,
  val firstName: String,
  val lastName: String
)