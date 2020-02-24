package com.hedvig.paymentservice.services.adyen.dtos

import com.hedvig.paymentservice.serviceIntergration.memberService.dto.Member

data class CardRegistrationRequest(
  val encryptedCardData: EncryptedCardData,
  val desiredCurrency: String,
  val member: Member
)
