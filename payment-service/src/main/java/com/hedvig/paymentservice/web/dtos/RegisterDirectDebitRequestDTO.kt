package com.hedvig.paymentservice.web.dtos


data class RegisterDirectDebitRequestDTO (
    val firstName: String,
    val lastName: String,
    val personalNumber: String
  )
