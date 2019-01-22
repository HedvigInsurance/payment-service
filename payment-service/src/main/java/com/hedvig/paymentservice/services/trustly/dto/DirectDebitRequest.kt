package com.hedvig.paymentservice.services.trustly.dto

class DirectDebitRequest(
  val firstName: String,
  val lastName: String,
  val ssn: String,
  val memberId: String,
  val triggerId: String
)
