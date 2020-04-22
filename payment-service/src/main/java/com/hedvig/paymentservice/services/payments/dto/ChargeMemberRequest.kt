package com.hedvig.paymentservice.services.payments.dto

import com.hedvig.paymentservice.web.dtos.ChargeRequest
import javax.money.MonetaryAmount

data class ChargeMemberRequest(
  var memberId: String,
  var amount: MonetaryAmount,
  var createdBy: String
) {
  companion object {
    fun fromChargeRequest(memberId: String, request: ChargeRequest): ChargeMemberRequest {
      return ChargeMemberRequest(memberId, request.amount, request.requestedBy)
    }
  }
}
