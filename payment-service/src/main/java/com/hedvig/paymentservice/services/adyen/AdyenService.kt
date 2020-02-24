package com.hedvig.paymentservice.services.adyen

import com.hedvig.paymentservice.services.adyen.dtos.CardRegistrationRequest

interface AdyenService {
  fun registerToken(req: CardRegistrationRequest): Any
  fun chargeMemberWithToken(): Any
  fun fetchCardDetails(): Any
}
