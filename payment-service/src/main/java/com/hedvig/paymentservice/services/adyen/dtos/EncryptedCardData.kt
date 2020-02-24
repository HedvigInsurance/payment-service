package com.hedvig.paymentservice.services.adyen.dtos

data class EncryptedCardData(
  val encryptedCardNumber: String,
  val encryptedExpiryMonth: String,
  val encryptedExpiryYear: String,
  val encryptedSecurityCode: String,
  val holderName: String
)
