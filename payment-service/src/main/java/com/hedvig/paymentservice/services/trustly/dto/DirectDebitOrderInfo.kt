package com.hedvig.paymentservice.services.trustly.dto

import com.hedvig.paymentservice.serviceIntergration.memberService.dto.Member
import com.hedvig.paymentservice.web.dtos.RegisterDirectDebitRequestDTO

data class DirectDebitOrderInfo(

    val memberId: String,
    val personalNumber: String,
    val firstName: String,
    val lastName: String,
    val triggerId: String? = null,
    val redirectingToBotService: Boolean = false
) {
  constructor(request: DirectDebitRequest, isRedirectingToBotService: Boolean) :
    this(
      request.memberId,
      request.ssn,
      request.firstName,
      request.lastName,
      request.triggerId,
      isRedirectingToBotService
    )

  constructor(
      memberId: String,
      request: RegisterDirectDebitRequestDTO,
      isRedirectingToBotService: Boolean
  ) : this(memberId, request.personalNumber, request.firstName, request.lastName, null, isRedirectingToBotService)

  companion object {
    fun fromMember(m: Member): DirectDebitOrderInfo {
      return DirectDebitOrderInfo(
        m.memberId,
        m.ssn,
        m.firstName,
        m.lastName,
        null,
        false
      )
    }
  }
}
