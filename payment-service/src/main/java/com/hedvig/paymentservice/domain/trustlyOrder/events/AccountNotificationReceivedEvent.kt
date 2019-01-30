package com.hedvig.paymentservice.domain.trustlyOrder.events

import java.util.*

data class AccountNotificationReceivedEvent(

  val hedvigOrderId: UUID,
  val memberId: String,

  val notificationId: String,
  val trustlyOrderId: String,

  val accountId: String,
  val address: String,
  val bank: String,
  val city: String,
  val clearingHouse: String,
  val descriptor: String,
  val directDebitMandate: Boolean?,
  val lastDigits: String,
  val name: String,
  val personId: String,
  val zipCode: String
)
