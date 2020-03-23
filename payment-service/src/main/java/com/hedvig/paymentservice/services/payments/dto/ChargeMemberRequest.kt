package com.hedvig.paymentservice.services.payments.dto

import javax.money.MonetaryAmount

data class ChargeMemberRequest(
  var memberId: String,
  var amount: MonetaryAmount,
  var createdBy: String
)
