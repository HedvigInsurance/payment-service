package com.hedvig.paymentservice.services.adyen.dtos

import java.util.UUID
import javax.money.MonetaryAmount

class ChargeMemberWithTokenRequest(
  val transactionId: UUID,
  val memberId: String,
  val recurringDetailReference: String,
  val amount: MonetaryAmount
)
