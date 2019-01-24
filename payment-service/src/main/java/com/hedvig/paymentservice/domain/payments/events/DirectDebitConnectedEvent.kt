package com.hedvig.paymentservice.domain.payments.events

data class DirectDebitConnectedEvent(
  val memberId: String,
  val hedvigOrderId: String,
  val trustlyAccountId: String
)
