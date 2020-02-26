package com.hedvig.paymentservice.services.adyen

import com.hedvig.paymentservice.services.adyen.dtos.CardRegistrationRequest
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberRequest

interface AdyenService {
  fun registerToken(req: CardRegistrationRequest): Any
  fun chargeMemberWithToken(req: ChargeMemberRequest): Any
  fun fetchCardDetails(memberId: String): Any
}
