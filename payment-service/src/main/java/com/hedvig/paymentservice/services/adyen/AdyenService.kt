package com.hedvig.paymentservice.services.adyen

interface AdyenService {
  fun registerToken(): Any
  fun chargeMemberWithToken(): Any
  fun fetchCardDetails(): Any
}
