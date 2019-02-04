package com.hedvig.paymentservice.domain.payments.events

data class DirectDebitDisconnectedEvent(

  val memberId: String,
  val hedvigOrderId: String,
  val trustlyAccountId: String
)
